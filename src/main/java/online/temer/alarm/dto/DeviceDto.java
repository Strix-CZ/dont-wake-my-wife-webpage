package online.temer.alarm.dto;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.TimeZone;
import java.util.stream.Collectors;

public class DeviceDto
{
	public final Long id;
	public final LocalDateTime timeCreated;
	public final TimeZone timeZone;
	public final String secretKey;

	public DeviceDto(LocalDateTime timeCreated, TimeZone timeZone, String secretKey)
	{
		this(null, timeCreated, timeZone, secretKey);
	}

	public DeviceDto(Long id, LocalDateTime timeCreated, TimeZone timeZone, String secretKey)
	{
		this.id = id;
		this.timeCreated = timeCreated;
		this.timeZone = timeZone;
		this.secretKey = secretKey;
	}

	public static DeviceDto generateDevice()
	{
		return generateDevice(TimeZone.getDefault());
	}

	public static DeviceDto generateDevice(TimeZone timeZone)
	{
		String possibleLetters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ01234567890";

		String secretKey = new SecureRandom().ints()
				.limit(40)
				.mapToObj(r -> "" + possibleLetters.charAt(Math.abs(r) % possibleLetters.length()))
				.collect(Collectors.joining());

		return new DeviceDto(
				LocalDateTime.now(),
				timeZone,
				secretKey
		);
	}

	public static class Query
	{
		private final Connection connection;

		public Query(Connection connection)
		{
			this.connection = connection;
		}

		public long insertDevice(DeviceDto device)
		{
			try
			{
				return new QueryRunner().query(connection,
						"INSERT INTO Device(timeCreated, timeZone, secretKey) "
								+ "VALUES (?, ?, ?) RETURNING id",
						new ScalarHandler<>(),
						device.timeCreated, device.timeZone.toZoneId().getId(), device.secretKey);
			}
			catch (SQLException e)
			{
				throw new RuntimeException(e);
			}
		}

		public DeviceDto get(long id)
		{
			try
			{
				return new QueryRunner().query(
						connection,
						"SELECT id, timeCreated, timeZone, secretKey "
								+ "FROM Device WHERE id = ?",
						new Handler(),
						id);
			}
			catch (SQLException e)
			{
				throw new RuntimeException(e);
			}
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
