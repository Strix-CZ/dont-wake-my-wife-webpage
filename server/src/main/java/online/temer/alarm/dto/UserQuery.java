package online.temer.alarm.dto;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;

public class UserQuery
{
	public UserDto get(Connection connection, String email)
	{
		try
		{
			return new QueryRunner().query(
					connection,
					"SELECT id, email, hash, salt FROM User WHERE email = ?",
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
					"INSERT INTO User(email, hash, salt) "
							+ "VALUES (?, ?, ?) RETURNING id",
					new ScalarHandler<>(),
					userDto.email, userDto.hash, userDto.salt);
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

	public UserDto createInsertAndLoadUser(Connection connection, String email, String password)
	{
		insert(connection, createUser(email, password));
		return get(connection, email);
	}

	public UserDto createUser(String email, String password)
	{
		String salt = generateSalt();
		String hash = getHash(password, salt);

		return new UserDto(email, hash, salt);
	}

	public String generateSalt()
	{
		byte[] salt = new byte[64];
		new SecureRandom().nextBytes(salt);
		return Base64.getEncoder().encodeToString(salt);
	}

	public String getHash(String password, String salt)
	{
		try
		{
			KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(StandardCharsets.UTF_8), 9847, 512);
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			byte[] hash = factory.generateSecret(spec).getEncoded();
			return Base64.getEncoder().encodeToString(hash);
		}
		catch (NoSuchAlgorithmException | InvalidKeySpecException e)
		{
			throw new RuntimeException(e);
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
					rs.getString("email"),
					rs.getString("hash"),
					rs.getString("salt")
			);
		}
	}
}
