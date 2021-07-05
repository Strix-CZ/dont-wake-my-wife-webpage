package online.temer.alarm.admin;

import online.temer.alarm.db.ConnectionProvider;
import online.temer.alarm.db.ProductionConnectionProvider;
import online.temer.alarm.dto.AlarmQuery;
import online.temer.alarm.dto.DeviceCheckInQuery;
import online.temer.alarm.dto.DeviceQuery;
import online.temer.alarm.dto.UserQuery;
import online.temer.alarm.server.ExceptionLogger;
import online.temer.alarm.server.NoOperationExceptionLogger;
import online.temer.alarm.server.Server;
import online.temer.alarm.server.authentication.DatabaseUserList;
import online.temer.alarm.server.authentication.DeviceAuthentication;
import online.temer.alarm.server.authentication.UserAuthentication;
import online.temer.alarm.server.handlers.CheckInHandler;
import online.temer.alarm.server.handlers.GetAlarmHandler;
import online.temer.alarm.server.handlers.SetAlarmHandler;

import java.sql.Connection;
import java.sql.SQLException;

public class Main
{
	public static void main(String[] args)
	{

		try (Connection connection = new ProductionConnectionProvider().get())
		{
			var management = new Management(connection, new UserQuery(), new DeviceQuery(), createServer());

			Output output = management.execute(args);

			System.out.println(String.join("\n", output.lines));
			System.exit(output.exitCode);
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	private static Server createServer()
	{
		ConnectionProvider connectionProvider = new ProductionConnectionProvider();
		DeviceQuery deviceQuery = new DeviceQuery();
		DeviceAuthentication deviceAuthentication = new DeviceAuthentication(deviceQuery);
		UserQuery userQuery = new UserQuery();
		DatabaseUserList userList = new DatabaseUserList(userQuery);
		UserAuthentication userAuthentication = new UserAuthentication(userList);
		AlarmQuery alarmQuery = new AlarmQuery();
		ExceptionLogger exceptionLogger = new NoOperationExceptionLogger();

		return new Server(
				new CheckInHandler(deviceAuthentication, alarmQuery, new DeviceCheckInQuery(), connectionProvider, exceptionLogger),
				new GetAlarmHandler(connectionProvider, userAuthentication, alarmQuery, exceptionLogger, deviceQuery),
				new SetAlarmHandler(connectionProvider, userAuthentication, alarmQuery, exceptionLogger, deviceQuery));
	}
}
