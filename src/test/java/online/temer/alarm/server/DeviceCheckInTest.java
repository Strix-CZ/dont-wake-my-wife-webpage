package online.temer.alarm.server;

import online.temer.alarm.db.DbTestExtension;
import online.temer.alarm.db.TestConnectionProvider;
import online.temer.alarm.dto.DeviceDto;
import online.temer.alarm.dto.DeviceCheckInDto;
import org.apache.http.client.utils.URIBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;

@ExtendWith(DbTestExtension.class)
public class DeviceCheckInTest
{
	private Server server;
	private DeviceDto device;

	@BeforeEach
	void setUp()
	{
		TestConnectionProvider testConnectionProvider = new TestConnectionProvider();

		server = new Server(8765, "localhost", testConnectionProvider);
		server.start();

		device = new DeviceDto.Query(testConnectionProvider.get())
				.insertDevice();
	}

	@AfterEach
	void tearDown()
	{
		server.stop();
	}

	@Test
	public void serverListens()
	{
		doCheckIn(0); // Throws in case of time-out
	}

	@Test
	public void checkIn_storesBattery()
	{
		doCheckIn(100);

		var before = LocalDateTime.now().withNano(0);
		var latestUpdate = new DeviceCheckInDto.Query(new TestConnectionProvider().get())
				.getLatest(device.id);
		var after = LocalDateTime.now().withNano(0);

		Assertions.assertNotNull(latestUpdate, "The check-in was not stored");
		Assertions.assertTrue(latestUpdate.device > 0, "id was less than 0");
		Assertions.assertEquals(device.id, latestUpdate.device, "device id");
		Assertions.assertEquals(100, latestUpdate.battery, "battery");
		Assertions.assertTrue(before.isBefore(latestUpdate.time) || before.isEqual(latestUpdate.time), "Time of update was too soon");
		Assertions.assertTrue(after.isAfter(latestUpdate.time) || after.isEqual(latestUpdate.time), "Time of update was too late");
	}

	private void doCheckIn(int battery)
	{
		try
		{
			URI uri = new URIBuilder("http://localhost:8765/checkin")
					.addParameter("device", Long.toString(device.id))
					.addParameter("battery", Integer.toString(battery))
					.build();

			var request = HttpRequest.newBuilder(uri).build();

			HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
		}
		catch (InterruptedException | IOException | URISyntaxException e)
		{
			throw new RuntimeException(e);
		}
	}
}
