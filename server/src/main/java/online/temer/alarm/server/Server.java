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


		Spark.options("/*",
				(request, response) -> {

					String accessControlRequestHeaders = request
							.headers("Access-Control-Request-Headers");
					if (accessControlRequestHeaders != null) {
						response.header("Access-Control-Allow-Headers",
								accessControlRequestHeaders);
					}

					String accessControlRequestMethod = request
							.headers("Access-Control-Request-Method");
					if (accessControlRequestMethod != null) {
						response.header("Access-Control-Allow-Methods",
								accessControlRequestMethod);
					}

					return "OK";
				});

		Spark.before((request, response) -> response.header("Access-Control-Allow-Origin", "*"));

		Spark.awaitInitialization();
	}

	public void stop()
	{
		Spark.stop();
		Spark.awaitStop();
	}
}
