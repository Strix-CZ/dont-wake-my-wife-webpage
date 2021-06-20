package online.temer.alarm.dto;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.TimeZone;

public class DeviceQuery {
	private final Connection connection;

	public DeviceQuery(Connection connection) {
		this.connection = connection;
	}

	public DeviceDto generateSaveAndLoadDevice() {
		long id = insertDevice(connection, DeviceDto.generateDevice());
		return get(id);
	}

	public long insertDevice(Connection connection, DeviceDto device) {
		try {
			return new QueryRunner().query(connection,
					"INSERT INTO Device(timeCreated, timeZone, secretKey) "
							+ "VALUES (?, ?, ?) RETURNING id",
					new ScalarHandler<>(),
					device.timeCreated, device.timeZone.toZoneId().getId(), device.secretKey);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public DeviceDto get(long id) {
		try {
			return new QueryRunner().query(
					connection,
					"SELECT id, timeCreated, timeZone, secretKey "
							+ "FROM Device WHERE id = ?",
					new Handler(),
					id);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private static class Handler implements ResultSetHandler<DeviceDto>
	{
		@Override
		public DeviceDto handle(ResultSet rs) throws SQLException
		{
			if (!rs.next())
			{
				return null;
			}

			return new DeviceDto(
					rs.getLong("id"),
					rs.getTimestamp("timeCreated").toLocalDateTime(),
					TimeZone.getTimeZone(ZoneId.of(rs.getString("timeZone"))),
					rs.getString("secretKey")
			);
		}
	}
}
