package online.temer.alarm.server;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.RoutingHandler;
import online.temer.alarm.db.ConnectionProvider;
import online.temer.alarm.dto.AlarmQuery;
import online.temer.alarm.dto.DeviceCheckInQuery;
import online.temer.alarm.server.device.CheckInHandler;
import online.temer.alarm.server.device.DeviceAuthentication;
import online.temer.alarm.server.ui.AlarmHandler;

public class Server
{
	private final Undertow server;
	private final ConnectionProvider connectionProvider;
	private final DeviceAuthentication deviceAuthentication;
	private final AlarmQuery alarmQuery;
	private final DeviceCheckInQuery deviceCheckInQuery;
	private final AlarmHandler alarmHandler;

	public Server(int port, String host, ConnectionProvider connectionProvider, DeviceAuthentication deviceAuthentication, AlarmQuery alarmQuery, DeviceCheckInQuery deviceCheckInQuery, AlarmHandler alarmHandler)
	{
		this.connectionProvider = connectionProvider;
		this.deviceAuthentication = deviceAuthentication;
		this.alarmQuery = alarmQuery;
		this.deviceCheckInQuery = deviceCheckInQuery;
		this.alarmHandler = alarmHandler;
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
		HttpHandler router = new RoutingHandler()
				.get("/checkin", new CheckInHandler(deviceAuthentication, alarmQuery, deviceCheckInQuery, connectionProvider))
				.get("/alarm", alarmHandler);

		return Undertow.builder()
				.addHttpListener(port, host)
				.setHandler(router)
				.build();
	}
}
