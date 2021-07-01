package online.temer.alarm.server.ui;

import online.temer.alarm.db.ConnectionProvider;
import online.temer.alarm.dto.AlarmDto;
import online.temer.alarm.dto.AlarmQuery;
import online.temer.alarm.dto.DeviceDto;
import online.temer.alarm.server.Handler;
import online.temer.alarm.server.QueryParameterReader;
import online.temer.alarm.server.authentication.UserAuthentication;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.time.LocalTime;

public class AlarmHandler extends Handler<DeviceDto>
{
	private final AlarmQuery alarmQuery;

	public AlarmHandler(ConnectionProvider connectionProvider, UserAuthentication userAuthentication, AlarmQuery alarmQuery)
	{
		super(connectionProvider, userAuthentication);
		this.alarmQuery = alarmQuery;
	}

	@Override
	protected Response handle(DeviceDto device, QueryParameterReader parameterReader, Connection connection)
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

	@Override
	protected Response handlePost(DeviceDto device, QueryParameterReader parameterReader, String body, Connection connection)
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
