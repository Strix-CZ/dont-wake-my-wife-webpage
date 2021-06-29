package online.temer.alarm.server;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.RoutingHandler;
import online.temer.alarm.server.device.CheckInHandler;
import online.temer.alarm.server.ui.AlarmHandler;

public class Server
{
	private final Undertow server;
	private final CheckInHandler checkInHandler;
	private final AlarmHandler alarmHandler;

	public Server(int port, String host, CheckInHandler checkInHandler, AlarmHandler alarmHandler)
	{
		this.checkInHandler = checkInHandler;
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
				.get("/checkin", checkInHandler)
				.post("/alarm", alarmHandler)
				.get("/alarm", alarmHandler);

		return Undertow.builder()
				.addHttpListener(port, host)
				.setHandler(router)
				.build();
	}
}
