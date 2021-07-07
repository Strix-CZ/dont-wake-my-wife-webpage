package online.temer.alarm.dto;

import online.temer.alarm.db.ListResultSetHandler;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.List;
import java.util.TimeZone;

public class DeviceQuery
{
	public DeviceDto generateSaveAndLoadDevice(Connection connection)
	{
		return generateSaveAndLoadDevice(connection, TimeZone.getDefault(), null);
	}

	public DeviceDto generateSaveAndLoadDevice(Connection connection, long owner)
	{
		return generateSaveAndLoadDevice(connection, TimeZone.getDefault(), owner);
	}

	public DeviceDto generateSaveAndLoadDevice(Connection connection, TimeZone timeZone, Long owner)
	{
		long id = insertDevice(connection, DeviceDto.generateDevice(timeZone, owner));
		return get(connection, id);
	}

	public long insertDevice(Connection connection, DeviceDto device)
	{
		try
		{
			return new QueryRunner().query(connection,
					"INSERT INTO Device(timeCreated, timeZone, secretKey, kOwner) "
							+ "VALUES (?, ?, ?, ?) RETURNING id",
					new ScalarHandler<>(),
					device.timeCreated, device.timeZone.toZoneId().getId(), device.secretKey, device.owner);
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	public DeviceDto get(Connection connection, long id)
	{
		try
		{
			return new QueryRunner().query(
					connection,
					"SELECT * FROM Device WHERE id = ?",
					new Handler(),
					id);
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	public List<DeviceDto> getByOwner(Connection connection, long owner)
	{
		try
		{
			return new QueryRunner().query(
					connection,
					"SELECT * FROM Device WHERE kOwner = ?",
					new ListResultSetHandler<>(new Handler()),
					owner);
		}
		catch (SQLException e)
		{
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
					rs.getString("secretKey"),
					rs.getLong("kOwner"));
		}
	}
}
