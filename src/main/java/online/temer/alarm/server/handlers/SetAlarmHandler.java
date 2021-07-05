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
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.time.LocalTime;

public class SetAlarmHandler extends Handler<UserDto>
{
	private final AlarmQuery alarmQuery;
	private final DeviceQuery deviceQuery;

	public SetAlarmHandler(ConnectionProvider connectionProvider, Authentication<UserDto> authentication, AlarmQuery alarmQuery, ExceptionLogger exceptionLogger, DeviceQuery deviceQuery)
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

		try
		{
			var object = new JSONObject(body);
			var time = LocalTime.of(object.getInt("hour"), object.getInt("minute"));
			var alarmDto = new AlarmDto(device.id, time);

			alarmQuery.insertOrUpdateAlarm(connection, alarmDto);

			return new Response(200);
		}
		catch (JSONException e)
		{
			return new Response(400, "incorrect JSON");
		}
	}
}
