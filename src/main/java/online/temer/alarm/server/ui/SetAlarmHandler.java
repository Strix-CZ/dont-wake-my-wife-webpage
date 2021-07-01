package online.temer.alarm.server.ui;

import online.temer.alarm.db.ConnectionProvider;
import online.temer.alarm.dto.AlarmDto;
import online.temer.alarm.dto.AlarmQuery;
import online.temer.alarm.dto.DeviceDto;
import online.temer.alarm.server.Handler;
import online.temer.alarm.server.QueryParameterReader;
import online.temer.alarm.server.authentication.Authentication;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.time.LocalTime;

public class SetAlarmHandler extends Handler<DeviceDto>
{
	private final AlarmQuery alarmQuery;

	public SetAlarmHandler(ConnectionProvider connectionProvider, Authentication<DeviceDto> authentication, AlarmQuery alarmQuery)
	{
		super(connectionProvider, authentication);
		this.alarmQuery = alarmQuery;
	}

	@Override
	protected Response handle(DeviceDto device, QueryParameterReader parameterReader, String body, Connection connection)
	{
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
