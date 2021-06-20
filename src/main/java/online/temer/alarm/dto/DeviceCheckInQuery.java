package online.temer.alarm.dto;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DeviceCheckInQuery {
	private final Connection connection;

	public DeviceCheckInQuery(Connection connection) {
		this.connection = connection;
	}

	public DeviceCheckInDto getLatest(Connection connection, long device) {
		try {
			return new QueryRunner().query(connection,
					"SELECT id, kDevice device, time, battery " +
							"FROM DeviceCheckIn " +
							"WHERE id = (SELECT MAX(id) FROM DeviceCheckIn WHERE kDevice = ?)",
					new Handler(),
					device);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public void insertUpdate(Connection connection, DeviceCheckInDto update) {
		try {
			new QueryRunner().update(connection,
					"INSERT INTO DeviceCheckIn(kDevice, time, battery) " +
							"VALUES (?, ?, ?)",
					update.device, update.time, update.battery);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private static class Handler implements ResultSetHandler<DeviceCheckInDto>
	{
		@Override
		public DeviceCheckInDto handle(ResultSet rs) throws SQLException
		{
			if (!rs.next())
			{
				return null;
			}

			return new DeviceCheckInDto(
					rs.getLong("id"),
					rs.getLong("kDevice"),
					rs.getTimestamp("time").toLocalDateTime(),
					rs.getInt("battery")
			);
		}
	}
}
