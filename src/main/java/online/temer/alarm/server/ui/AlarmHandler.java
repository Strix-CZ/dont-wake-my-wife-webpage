package online.temer.alarm.server.ui;

import online.temer.alarm.db.ConnectionProvider;
import online.temer.alarm.dto.AlarmDto;
import online.temer.alarm.dto.AlarmQuery;
import online.temer.alarm.server.Handler;
import online.temer.alarm.server.QueryParameterReader;

import java.sql.Connection;

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
		var device = userAuthentication.authenticate(connection);
		if (device.isEmpty())
		{
			return new Response(401);
		}

		AlarmDto alarm = alarmQuery.get(connection, device.get().id);
		if (alarm == null)
		{
			return new Response(200, "{}");
		}
		else {
			return new Response(200,
					"{hour:" + alarm.time.getHour() + ","
							+"minute:"+ alarm.time.getMinute()+"}");
		}
	}
}
