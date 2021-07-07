package online.temer.alarm.server;

import online.temer.alarm.server.handlers.CheckInHandler;
import online.temer.alarm.server.handlers.GetAlarmHandler;
import online.temer.alarm.server.handlers.SetAlarmHandler;
import spark.Spark;

public class Server
{
	private final CheckInHandler checkInHandler;
	private final GetAlarmHandler getAlarmHandler;
	private final SetAlarmHandler setAlarmHandler;

	public Server(CheckInHandler checkInHandler, GetAlarmHandler getAlarmHandler, SetAlarmHandler setAlarmHandler)
	{
		this.checkInHandler = checkInHandler;
		this.getAlarmHandler = getAlarmHandler;
		this.setAlarmHandler = setAlarmHandler;
	}

	public void start(int port, String host)
	{
		Spark.ipAddress(host);
		Spark.port(port);

		Spark.get("/checkin", checkInHandler);
		Spark.get("/alarm", getAlarmHandler);
		Spark.post("/alarm", setAlarmHandler);

		Spark.awaitInitialization();
	}

	public void stop()
	{
		Spark.stop();
		Spark.awaitStop();
	}
}
