package online.temer.alarm.server;

import online.temer.alarm.db.ConnectionProvider;
import online.temer.alarm.db.DbTestExtension;
import online.temer.alarm.db.TestConnectionProvider;
import online.temer.alarm.dto.AlarmQuery;
import online.temer.alarm.dto.DeviceCheckInQuery;
import online.temer.alarm.dto.DeviceQuery;
import online.temer.alarm.server.device.CheckInHandler;
import online.temer.alarm.server.device.DeviceAuthentication;
import online.temer.alarm.server.ui.AlarmHandler;
import online.temer.alarm.server.ui.UserAuthentication;
import org.junit.jupiter.api.extension.ExtensionContext;

public class ServerTestExtension extends DbTestExtension
{
	private Server server;

	@Override
	public void beforeAll(ExtensionContext context)
	{
		super.beforeAll(context);

		ConnectionProvider connectionProvider = new TestConnectionProvider();
		DeviceQuery deviceQuery = new DeviceQuery();

		server = new Server(
				8765,
				"localhost",
				new CheckInHandler(new DeviceAuthentication(deviceQuery), new AlarmQuery(), new DeviceCheckInQuery(), connectionProvider),
				new AlarmHandler(connectionProvider, new UserAuthentication(deviceQuery)));

		server.start();
	}

	@Override
	public void afterAll(ExtensionContext extensionContext)
	{
		server.stop();
		super.afterAll(extensionContext);
	}
}
