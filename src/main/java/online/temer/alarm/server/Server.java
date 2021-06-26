package online.temer.alarm.server;

import io.undertow.Handlers;
import io.undertow.Undertow;
import online.temer.alarm.db.ConnectionProvider;
import online.temer.alarm.dto.AlarmQuery;
import online.temer.alarm.dto.DeviceCheckInQuery;
import online.temer.alarm.server.device.CheckInHandler;
import online.temer.alarm.server.device.DeviceAuthentication;

public class Server
{
	private final Undertow server;
	private final ConnectionProvider connectionProvider;
	private final DeviceAuthentication deviceAuthentication;
	private final AlarmQuery alarmQuery;
	private final DeviceCheckInQuery deviceCheckInQuery;

	public Server(int port, String host, ConnectionProvider connectionProvider, DeviceAuthentication deviceAuthentication, AlarmQuery alarmQuery, DeviceCheckInQuery deviceCheckInQuery)
	{
		this.connectionProvider = connectionProvider;
		this.deviceAuthentication = deviceAuthentication;
		this.alarmQuery = alarmQuery;
		this.deviceCheckInQuery = deviceCheckInQuery;
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
						"/checkin", new CheckInHandler(deviceAuthentication, alarmQuery, deviceCheckInQuery, connectionProvider)))
				.build();
	}
}
