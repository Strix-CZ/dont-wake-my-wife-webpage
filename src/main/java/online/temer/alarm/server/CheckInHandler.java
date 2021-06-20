package online.temer.alarm.server;

import online.temer.alarm.db.ConnectionProvider;
import online.temer.alarm.dto.*;
import online.temer.alarm.util.DateTimeUtil;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CheckInHandler extends Handler
{
	private final DeviceAuthentication deviceAuthentication;

	public CheckInHandler(DeviceAuthentication deviceAuthentication, ConnectionProvider connectionProvider)
	{
		super(connectionProvider);
		this.deviceAuthentication = deviceAuthentication;
	}

	public Response handle(QueryParameterReader parameterReader, Connection connection)
	{
		DeviceDto device = deviceAuthentication.authenticate(connection, parameterReader);

		int battery = parameterReader.readInt("battery");
		logCheckIn(connection, device.id, battery);

		AlarmDto alarm = new AlarmQuery().get(connection, device.id);

		return new Response(DateTimeUtil.formatCurrentTime(device.timeZone) + "\n"
				+ formatAlarm(alarm) + "\n");
	}

	private void logCheckIn(Connection connection, long deviceId, int battery)
	{
		var deviceCheckInDto = new DeviceCheckInDto(deviceId, LocalDateTime.now(), battery);
		new DeviceCheckInQuery()
				.insertUpdate(connection, deviceCheckInDto);
	}

	private String formatAlarm(AlarmDto alarm) {
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
