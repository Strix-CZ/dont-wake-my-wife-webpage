package online.temer.alarm.server.handlers;

import online.temer.alarm.db.ConnectionProvider;
import online.temer.alarm.dto.AlarmDto;
import online.temer.alarm.dto.AlarmQuery;
import online.temer.alarm.dto.DeviceDto;
import online.temer.alarm.dto.DeviceQuery;
import online.temer.alarm.dto.UserDto;
import online.temer.alarm.server.ExceptionLogger;
import online.temer.alarm.server.Handler;
import online.temer.alarm.server.QueryParameterReader;
import online.temer.alarm.server.authentication.Authentication;

import java.sql.Connection;

public class GetAlarmHandler extends Handler<UserDto>
{
	private final AlarmQuery alarmQuery;
	private final DeviceQuery deviceQuery;

	public GetAlarmHandler(ConnectionProvider connectionProvider, Authentication<UserDto> authentication, AlarmQuery alarmQuery, ExceptionLogger exceptionLogger, DeviceQuery deviceQuery)
	{
		super(connectionProvider, authentication, exceptionLogger);
		this.alarmQuery = alarmQuery;
		this.deviceQuery = deviceQuery;
	}

	@Override
	protected Response handle(UserDto user, QueryParameterReader parameterReader, String body, Connection connection)
	{
		DeviceDto device = deviceQuery.get(connection);
		if (device == null)
		{
			return new Response(400);
		}

		AlarmDto alarm = alarmQuery.get(connection, device.id);
		if (alarm == null)
		{
			return new Response(200, "{}");
		}
		else
		{
			return new Response(200,
					"{hour:" + alarm.time.getHour() + ","
							+ "minute:" + alarm.time.getMinute() + "}");
		}
	}
}
