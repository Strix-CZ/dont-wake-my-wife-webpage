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
import java.util.List;

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
		List<DeviceDto> deviceList = deviceQuery.getByOwner(connection, user.id);
		if (deviceList.isEmpty())
		{
			return new Response(400);
		}
		DeviceDto device = deviceList.get(0);

		try
		{
			JSONObject object = new JSONObject(body);
			boolean isActive = object.getBoolean("isActive");
			LocalTime time = LocalTime.of(object.getInt("hour"), object.getInt("minute"));
			AlarmDto alarmDto = new AlarmDto(device.id, isActive, time);

			alarmQuery.insertOrUpdate(connection, alarmDto);

			return new Response(200);
		}
		catch (JSONException e)
		{
			return new Response(400, "incorrect JSON");
		}
	}
}
