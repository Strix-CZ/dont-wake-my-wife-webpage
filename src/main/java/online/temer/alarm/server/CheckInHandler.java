package online.temer.alarm.server;

import online.temer.alarm.db.ConnectionProvider;
import online.temer.alarm.dto.AlarmDto;
import online.temer.alarm.dto.AlarmQuery;
import online.temer.alarm.dto.DeviceCheckInDto;
import online.temer.alarm.dto.DeviceCheckInQuery;
import online.temer.alarm.dto.DeviceDto;
import online.temer.alarm.util.DateTimeUtil;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CheckInHandler extends Handler
{
	private final DeviceAuthentication deviceAuthentication;
	private final AlarmQuery alarmQuery;
	private final DeviceCheckInQuery deviceCheckInQuery;

	public CheckInHandler(DeviceAuthentication deviceAuthentication, AlarmQuery alarmQuery, DeviceCheckInQuery deviceCheckInQuery, ConnectionProvider connectionProvider)
	{
		super(connectionProvider);
		this.deviceAuthentication = deviceAuthentication;
		this.alarmQuery = alarmQuery;
		this.deviceCheckInQuery = deviceCheckInQuery;
	}

	public Response handle(QueryParameterReader parameterReader, Connection connection)
	{
		DeviceAuthentication.Result authenticationResult = deviceAuthentication.authenticate(connection, parameterReader);
		if (authenticationResult.device.isEmpty())
		{
			return authenticationResult.errorResponse;
		}
		DeviceDto device = authenticationResult.device.get();

		int battery = parameterReader.readInt("battery");
		logCheckIn(connection, device.id, battery);

		AlarmDto alarm = alarmQuery.get(connection, device.id);

		return new Response(DateTimeUtil.formatCurrentTime(device.timeZone) + "\n"
				+ formatAlarm(alarm) + "\n");
	}

	private void logCheckIn(Connection connection, long deviceId, int battery)
	{
		var deviceCheckInDto = new DeviceCheckInDto(deviceId, LocalDateTime.now(), battery);
		deviceCheckInQuery.insertUpdate(connection, deviceCheckInDto);
	}

	private String formatAlarm(AlarmDto alarm)
	{
		if (alarm == null)
		{
			return "none";
		}
		else
		{
			return alarm.time.format(DateTimeFormatter.ISO_LOCAL_TIME);
		}
	}
}
