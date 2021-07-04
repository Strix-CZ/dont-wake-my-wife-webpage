package online.temer.alarm.server.handlers;

import online.temer.alarm.db.ConnectionProvider;
import online.temer.alarm.dto.AlarmDto;
import online.temer.alarm.dto.AlarmQuery;
import online.temer.alarm.dto.DeviceDto;
import online.temer.alarm.server.ExceptionLogger;
import online.temer.alarm.server.Handler;
import online.temer.alarm.server.QueryParameterReader;
import online.temer.alarm.server.authentication.Authentication;

import java.sql.Connection;

public class GetAlarmHandler extends Handler<DeviceDto>
{
	private final AlarmQuery alarmQuery;

	public GetAlarmHandler(ConnectionProvider connectionProvider, Authentication<DeviceDto> authentication, AlarmQuery alarmQuery, ExceptionLogger exceptionLogger)
	{
		super(connectionProvider, authentication, exceptionLogger);
		this.alarmQuery = alarmQuery;
	}

	@Override
	protected Response handle(DeviceDto device, QueryParameterReader parameterReader, String body, Connection connection)
	{
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
