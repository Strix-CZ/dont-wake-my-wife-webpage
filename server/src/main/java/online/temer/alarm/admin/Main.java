package online.temer.alarm.admin;

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

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

public class Main
{
	public static void main(String[] args)
	{
		var configurationReader = new ConfigurationReader(new File("db-config"));

		ProductionConnectionProvider connectionProvider = new ProductionConnectionProvider(configurationReader.readPassword());

		try (Connection connection = connectionProvider.get())
		{
			var management = new Management(connection, new UserQuery(), new DeviceQuery(), createServer(connectionProvider));

			Output output = management.execute(args);

			System.out.println(String.join("\n", output.lines));
			System.exit(output.exitCode);
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	private static Server createServer(ProductionConnectionProvider connectionProvider)
	{
		DeviceQuery deviceQuery = new DeviceQuery();
		DeviceAuthentication deviceAuthentication = new DeviceAuthentication(deviceQuery);
		UserQuery userQuery = new UserQuery();
		DatabaseUserList userList = new DatabaseUserList(userQuery);
		UserAuthentication userAuthentication = new UserAuthentication(userList);
		AlarmQuery alarmQuery = new AlarmQuery();
		DeviceCheckInQuery deviceCheckInQuery = new DeviceCheckInQuery();
		ExceptionLogger exceptionLogger = new NoOperationExceptionLogger();

		return new Server(
				new CheckInHandler(deviceAuthentication, alarmQuery, deviceCheckInQuery, connectionProvider, exceptionLogger),
				new GetAlarmHandler(connectionProvider, userAuthentication, alarmQuery, exceptionLogger, deviceQuery, deviceCheckInQuery),
				new SetAlarmHandler(connectionProvider, userAuthentication, alarmQuery, exceptionLogger, deviceQuery));
	}
}
