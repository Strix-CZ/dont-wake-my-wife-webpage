package online.temer.alarm.dto;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserQuery
{
	public UserDto get(Connection connection, String email)
	{
		try
		{
			return new QueryRunner().query(
					connection,
					"SELECT id, email FROM User WHERE email = ?",
					new UserQuery.Handler(),
					email);
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	public long insert(Connection connection, UserDto userDto)
	{
		try
		{
			return new QueryRunner().query(connection,
					"INSERT INTO User(email) "
							+ "VALUES (?) RETURNING id",
					new ScalarHandler<>(),
					userDto.email);
		}
		catch (SQLException e)
		{
			if (e.getErrorCode() == 1062)
			{
				throw new DuplicateEmailException("User with the same e-mail already exists.");
			}
			else
			{
				throw new RuntimeException(e);
			}
		}
	}

	public static class DuplicateEmailException extends RuntimeException
	{
		public DuplicateEmailException(String message)
		{
			super(message);
		}
	}

	private static class Handler implements ResultSetHandler<UserDto>
	{
		@Override
		public UserDto handle(ResultSet rs) throws SQLException
		{
			if (!rs.next())
			{
				return null;
			}

			return new UserDto(
					rs.getLong("id"),
					rs.getString("email")
			);
		}
	}
}
