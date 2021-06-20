package online.temer.alarm.server;

import io.undertow.Handlers;
import io.undertow.Undertow;
import online.temer.alarm.db.ConnectionProvider;

public class Server
{
	private final Undertow server;
	private final ConnectionProvider connectionProvider;
	private final DeviceAuthentication deviceAuthentication;

	public Server(int port, String host, ConnectionProvider connectionProvider, DeviceAuthentication deviceAuthentication)
	{
		this.connectionProvider = connectionProvider;
		this.deviceAuthentication = deviceAuthentication;
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
						"/checkin", new CheckInHandler(deviceAuthentication, connectionProvider)))
				.build();
	}
}
