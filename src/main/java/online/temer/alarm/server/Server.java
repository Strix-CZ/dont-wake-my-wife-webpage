package online.temer.alarm.server;

import io.undertow.Handlers;
import io.undertow.Undertow;
import online.temer.alarm.db.ConnectionProvider;
import online.temer.alarm.dto.AlarmQuery;

public class Server
{
	private final Undertow server;
	private final ConnectionProvider connectionProvider;
	private final DeviceAuthentication deviceAuthentication;
	private final AlarmQuery alarmQuery;

	public Server(int port, String host, ConnectionProvider connectionProvider, DeviceAuthentication deviceAuthentication, AlarmQuery alarmQuery)
	{
		this.connectionProvider = connectionProvider;
		this.deviceAuthentication = deviceAuthentication;
		this.alarmQuery = alarmQuery;
		server = createServer(port, host);
	}

	public void start()
	{
		server.start();
	}

	public void stop()
	{
		server.stop();
	}

	private Undertow createServer(int port, String host)
	{
		return Undertow.builder()
				.addHttpListener(port, host)
				.setHandler(Handlers.path().addExactPath(
						"/checkin", new CheckInHandler(deviceAuthentication, alarmQuery, connectionProvider)))
				.build();
	}
}
