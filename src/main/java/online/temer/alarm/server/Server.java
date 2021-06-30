package online.temer.alarm.server;

import online.temer.alarm.server.device.CheckInHandler;
import online.temer.alarm.server.ui.AlarmHandler;
import spark.Spark;

public class Server
{
	private final CheckInHandler checkInHandler;
	private final AlarmHandler alarmHandler;

	public Server(int port, String host, CheckInHandler checkInHandler, AlarmHandler alarmHandler)
	{
		this.checkInHandler = checkInHandler;
		this.alarmHandler = alarmHandler;

		createServer(port, host);
	}

	public void stop()
	{
		Spark.stop();
	}

	private void createServer(int port, String host)
	{
		Spark.ipAddress(host);
		Spark.port(port);

		Spark.get("/checkin", checkInHandler);
		Spark.get("/alarm", alarmHandler);
		Spark.post("/alarm", alarmHandler);
	}
}
