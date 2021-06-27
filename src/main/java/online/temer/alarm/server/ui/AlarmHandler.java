package online.temer.alarm.server.ui;

import online.temer.alarm.db.ConnectionProvider;
import online.temer.alarm.server.Handler;
import online.temer.alarm.server.QueryParameterReader;

import java.sql.Connection;

public class AlarmHandler extends Handler
{
	public AlarmHandler(ConnectionProvider connectionProvider)
	{
		super(connectionProvider);
	}

	@Override
	protected Response handle(QueryParameterReader parameterReader, Connection connection)
	{
		return new Response(200, "{}");
	}
}
