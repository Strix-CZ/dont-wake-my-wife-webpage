package online.temer.alarm.server.handlers;

import online.temer.alarm.db.ConnectionProvider;
import online.temer.alarm.dto.AlarmDto;
import online.temer.alarm.dto.AlarmQuery;
import online.temer.alarm.dto.DeviceCheckInDto;
import online.temer.alarm.dto.DeviceCheckInQuery;
import online.temer.alarm.dto.DeviceDto;
import online.temer.alarm.dto.DeviceQuery;
import online.temer.alarm.dto.UserDto;
import online.temer.alarm.server.ExceptionLogger;
import online.temer.alarm.server.Handler;
import online.temer.alarm.server.QueryParameterReader;
import online.temer.alarm.server.authentication.Authentication;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

public class GetAlarmHandler extends Handler<UserDto>
{
	private final AlarmQuery alarmQuery;
	private final DeviceQuery deviceQuery;
	private final DeviceCheckInQuery deviceCheckInQuery;

	public GetAlarmHandler(ConnectionProvider connectionProvider, Authentication<UserDto> authentication, AlarmQuery alarmQuery, ExceptionLogger exceptionLogger, DeviceQuery deviceQuery, DeviceCheckInQuery deviceCheckInQuery)
	{
		super(connectionProvider, authentication, exceptionLogger);
		this.alarmQuery = alarmQuery;
		this.deviceQuery = deviceQuery;
		this.deviceCheckInQuery = deviceCheckInQuery;
	}

	@Override
	protected Response handle(UserDto user, QueryParameterReader parameterReader, String body, Connection connection)
	{
		List<DeviceDto> deviceList = deviceQuery.getByOwner(connection, user.id);
		if (deviceList.isEmpty())
		{
			return new Response(400);
		}
		DeviceDto device = deviceList.get(0);

		return new Response(
				"{\"alarm\":" + getAlarm(connection, device) + "," +
						"\"checkIns\": " + getCheckIns(connection, device) +
						"}");
	}

	private String getAlarm(Connection connection, DeviceDto device)
	{
		AlarmDto alarm = alarmQuery.get(connection, device.id);
		if (alarm == null)
		{
			return "{}";
		}
		else
		{
			return "{\"isActive\":" + alarm.isActive + ","
					+ "\"hour\":" + alarm.time.getHour() + ","
					+ "\"minute\":" + alarm.time.getMinute() + "}";
		}
	}

	private String getCheckIns(Connection connection, DeviceDto device)
	{
		DeviceCheckInDto latestCheckIn = deviceCheckInQuery.getLatest(connection, device.id);
		List<DeviceCheckInDto> checkIns = latestCheckIn != null
				? Collections.singletonList(latestCheckIn)
				: Collections.emptyList();

		return "[" +
				checkIns.stream()
						.map(c -> checkInToJson(device.timeZone, c))
						.collect(Collectors.joining(","))
				+ "]";
	}

	private String checkInToJson(TimeZone deviceTimeZone, DeviceCheckInDto checkIn)
	{
		var timeInServerTimeZone = checkIn.time.atZone(ZoneId.systemDefault());
		var userTime = timeInServerTimeZone.withZoneSameInstant(deviceTimeZone.toZoneId());

		String time = userTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

		return "{\"time\":\"" + time + "\"," +
				"\"battery\":" + checkIn.battery + "}";
	}
}
