package online.temer.alarm.server;

import online.temer.alarm.db.ConnectionProvider;
import online.temer.alarm.db.DbTestExtension;
import online.temer.alarm.db.TestConnectionProvider;
import online.temer.alarm.dto.AlarmQuery;
import online.temer.alarm.dto.DeviceCheckInQuery;
import online.temer.alarm.server.device.CheckInHandler;
import online.temer.alarm.server.ui.GetAlarmHandler;
import online.temer.alarm.server.ui.SetAlarmHandler;
import online.temer.alarm.util.TestAuthentication;
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
		TestAuthentication authentication = new TestAuthentication();
		AlarmQuery alarmQuery = new AlarmQuery();
		ExceptionLogger exceptionLogger = new TestExceptionLogger();

		server = new Server(
				8765,
				"localhost",
				new CheckInHandler(authentication, alarmQuery, new DeviceCheckInQuery(), connectionProvider, exceptionLogger),
				new GetAlarmHandler(connectionProvider, authentication, alarmQuery, exceptionLogger),
				new SetAlarmHandler(connectionProvider, authentication, alarmQuery, exceptionLogger));
	}

	@Override
	public void afterEach(ExtensionContext extensionContext) throws SQLException
	{
		super.afterEach(extensionContext);

		TestAuthentication.setAuthenticationUndefined();
	}

	@Override
	public void afterAll(ExtensionContext extensionContext)
	{
		server.stop();
		super.afterAll(extensionContext);
	}
}
