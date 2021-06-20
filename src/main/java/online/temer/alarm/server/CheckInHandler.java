package online.temer.alarm.server;

import online.temer.alarm.db.ConnectionProvider;
import online.temer.alarm.dto.AlarmDto;
import online.temer.alarm.dto.DeviceCheckInDto;
import online.temer.alarm.dto.DeviceCheckInQuery;
import online.temer.alarm.dto.DeviceDto;
import online.temer.alarm.util.DateTimeUtil;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CheckInHandler extends Handler
{
	public CheckInHandler(ConnectionProvider connectionProvider)
	{
		super(connectionProvider);
	}

	public Response handle(QueryParameterReader parameterReader, Connection connection)
	{
		DeviceDto device = new DeviceAuthentication(connection).authenticate(parameterReader);

		int battery = parameterReader.readInt("battery");
		logCheckIn(connection, device.id, battery);

		AlarmDto alarm = new AlarmDto.Query(connection).get(device.id);

		return new Response(DateTimeUtil.formatCurrentTime(device.timeZone) + "\n"
				+ formatAlarm(alarm) + "\n");
	}

	private void logCheckIn(Connection connection, long deviceId, int battery)
	{
		var deviceCheckInDto = new DeviceCheckInDto(deviceId, LocalDateTime.now(), battery);
		new DeviceCheckInQuery(connection)
				.insertUpdate(deviceCheckInDto);
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
