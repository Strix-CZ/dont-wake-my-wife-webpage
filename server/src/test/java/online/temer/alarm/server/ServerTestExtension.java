package online.temer.alarm.server;

import online.temer.alarm.db.ConnectionProvider;
import online.temer.alarm.db.DbTestExtension;
import online.temer.alarm.db.TestConnectionProvider;
import online.temer.alarm.dto.AlarmQuery;
import online.temer.alarm.dto.DeviceCheckInQuery;
import online.temer.alarm.dto.DeviceQuery;
import online.temer.alarm.server.handlers.CheckInHandler;
import online.temer.alarm.server.handlers.GetAlarmHandler;
import online.temer.alarm.server.handlers.SetAlarmHandler;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.sql.SQLException;

public class ServerTestExtension extends DbTestExtension
{
	private Server server;

	@Override
	public void beforeAll(ExtensionContext context)
	{
		super.beforeAll(context);

		ConnectionProvider connectionProvider = new TestConnectionProvider();
		TestDeviceAuthentication deviceAuthentication = new TestDeviceAuthentication();
		TestUserAuthentication userAuthentication = new TestUserAuthentication();
		AlarmQuery alarmQuery = new AlarmQuery();
		DeviceQuery deviceQuery = new DeviceQuery();
		ExceptionLogger exceptionLogger = new TestExceptionLogger();

		server = new Server(
				new CheckInHandler(deviceAuthentication, alarmQuery, new DeviceCheckInQuery(), connectionProvider, exceptionLogger),
				new GetAlarmHandler(connectionProvider, userAuthentication, alarmQuery, exceptionLogger, deviceQuery),
				new SetAlarmHandler(connectionProvider, userAuthentication, alarmQuery, exceptionLogger, deviceQuery));

		server.start(8765, "localhost");
	}

	@Override
	public void afterEach(ExtensionContext extensionContext) throws SQLException
	{
		super.afterEach(extensionContext);

		TestDeviceAuthentication.setAuthenticationUndefined();
		TestUserAuthentication.setAuthenticationUndefined();
	}

	@Override
	public void afterAll(ExtensionContext extensionContext)
	{
		server.stop();
		super.afterAll(extensionContext);
	}
}
