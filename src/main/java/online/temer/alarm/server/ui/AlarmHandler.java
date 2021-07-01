package online.temer.alarm.server.ui;

import online.temer.alarm.db.ConnectionProvider;
import online.temer.alarm.dto.AlarmDto;
import online.temer.alarm.dto.AlarmQuery;
import online.temer.alarm.server.Handler;
import online.temer.alarm.server.QueryParameterReader;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.time.LocalTime;

public class AlarmHandler extends Handler
{
	private final UserAuthentication userAuthentication;
	private final AlarmQuery alarmQuery;

	public AlarmHandler(ConnectionProvider connectionProvider, UserAuthentication userAuthentication, AlarmQuery alarmQuery)
	{
		super(connectionProvider);
		this.userAuthentication = userAuthentication;
		this.alarmQuery = alarmQuery;
	}

	@Override
	protected Response handle(QueryParameterReader parameterReader, Connection connection)
	{
		var result = userAuthentication.authenticate(connection, parameterReader);
		if (result.entity.isEmpty())
		{
			return new Response(401);
		}

		AlarmDto alarm = alarmQuery.get(connection, result.entity.get().id);
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
	protected Response handlePost(QueryParameterReader parameterReader, String body, Connection connection)
	{
		var result = userAuthentication.authenticate(connection, parameterReader);
		if (result.entity.isEmpty())
		{
			return new Response(401);
		}

		try
		{
			var object = new JSONObject(body);
			var time = LocalTime.of(object.getInt("hour"), object.getInt("minute"));
			var alarmDto = new AlarmDto(result.entity.get().id, time);

			alarmQuery.insertOrUpdateAlarm(connection, alarmDto);

			return new Response(200);
		}
		catch (JSONException e)
		{
			return new Response(400, "incorrect JSON");
		}
	}
}
