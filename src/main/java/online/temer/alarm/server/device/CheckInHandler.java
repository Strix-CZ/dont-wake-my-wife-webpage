package online.temer.alarm.server.device;

import online.temer.alarm.db.ConnectionProvider;
import online.temer.alarm.dto.AlarmDto;
import online.temer.alarm.dto.AlarmQuery;
import online.temer.alarm.dto.DeviceCheckInDto;
import online.temer.alarm.dto.DeviceCheckInQuery;
import online.temer.alarm.dto.DeviceDto;
import online.temer.alarm.server.Handler;
import online.temer.alarm.server.QueryParameterReader;
import online.temer.alarm.server.authentication.DeviceAuthentication;
import online.temer.alarm.util.DateTimeUtil;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CheckInHandler extends Handler<DeviceDto>
{
	private final AlarmQuery alarmQuery;
	private final DeviceCheckInQuery deviceCheckInQuery;

	public CheckInHandler(DeviceAuthentication deviceAuthentication, AlarmQuery alarmQuery, DeviceCheckInQuery deviceCheckInQuery, ConnectionProvider connectionProvider)
	{
		super(connectionProvider, deviceAuthentication);
		this.alarmQuery = alarmQuery;
		this.deviceCheckInQuery = deviceCheckInQuery;
	}

	public Response handle(DeviceDto device, QueryParameterReader parameterReader, Connection connection)
	{
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
