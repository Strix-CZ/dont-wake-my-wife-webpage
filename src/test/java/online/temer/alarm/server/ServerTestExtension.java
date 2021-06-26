package online.temer.alarm.server;

import online.temer.alarm.db.DbTestExtension;
import online.temer.alarm.db.TestConnectionProvider;
import online.temer.alarm.dto.AlarmQuery;
import online.temer.alarm.dto.DeviceCheckInQuery;
import online.temer.alarm.dto.DeviceQuery;
import online.temer.alarm.server.device.DeviceAuthentication;
import org.junit.jupiter.api.extension.ExtensionContext;

public class ServerTestExtension extends DbTestExtension
{
	private Server server;

	@Override
	public void beforeAll(ExtensionContext context)
	{
		super.beforeAll(context);

		server = new Server(
				8765,
				"localhost",
				new TestConnectionProvider(),
				new DeviceAuthentication(new DeviceQuery()),
				new AlarmQuery(),
				new DeviceCheckInQuery());

		server.start();
	}

	@Override
	public void afterAll(ExtensionContext extensionContext)
	{
		server.stop();
		super.afterAll(extensionContext);
	}
}
