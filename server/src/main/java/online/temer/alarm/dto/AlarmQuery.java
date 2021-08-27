package online.temer.alarm.dto;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AlarmQuery
{
	public void insertOrUpdate(Connection connection, AlarmDto alarm)
	{
		try
		{
			new QueryRunner().update(connection,
					"INSERT INTO Alarm(kDevice, isActive, time) "
							+ "VALUES (?, ?, ?) "
							+ "ON DUPLICATE KEY UPDATE "
							+ "isActive = ?, time = ?",
					alarm.device, alarm.isActive, alarm.time, alarm.isActive, alarm.time);
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	public AlarmDto get(Connection connection, long device)
	{
		try
		{
			return new QueryRunner().query(
					connection,
					"SELECT * FROM Alarm WHERE kDevice = ?",
					new Handler(),
					device);
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	public void delete(Connection connection, long device)
	{
		try
		{
			new QueryRunner().update(connection,
					"DELETE FROM Alarm WHERE kDevice = ? ",
					device);
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	private static class Handler implements ResultSetHandler<AlarmDto>
	{
		@Override
		public AlarmDto handle(ResultSet rs) throws SQLException
		{
			if (!rs.next())
			{
				return null;
			}

			return new AlarmDto(
					rs.getLong("kDevice"),
					rs.getBoolean("isActive"),
					rs.getTimestamp("time").toLocalDateTime().toLocalTime());
		}
	}
}
