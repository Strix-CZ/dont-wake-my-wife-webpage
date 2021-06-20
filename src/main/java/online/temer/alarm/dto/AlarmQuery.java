package online.temer.alarm.dto;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AlarmQuery {
	private final Connection connection;

	public AlarmQuery(Connection connection) {
		this.connection = connection;
	}

	public void insertOrUpdateAlarm(Connection connection, AlarmDto alarm) {
		try {
			new QueryRunner().update(connection,
					"INSERT INTO Alarm(kDevice, time) "
							+ "VALUES (?, ?) "
							+ "ON DUPLICATE KEY UPDATE "
							+ "time = ?",
					alarm.device, alarm.time, alarm.time);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public AlarmDto get(long device) {
		try {
			return new QueryRunner().query(
					connection,
					"SELECT kDevice, time "
							+ "FROM Alarm WHERE kDevice = ?",
					new Handler(),
					device);
		} catch (SQLException e) {
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
					rs.getTimestamp("time").toLocalDateTime().toLocalTime()
			);
		}
	}
}
