package online.temer.alarm.server;

import online.temer.alarm.db.DbTestExtension;
import online.temer.alarm.db.TestConnectionProvider;
import online.temer.alarm.dto.DeviceCheckInDto;
import online.temer.alarm.dto.DeviceDto;
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
import java.time.format.DateTimeFormatter;

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

		var query = new DeviceDto.Query(testConnectionProvider.get());
		long id = query.insertDevice(DeviceDto.generateDevice());
		device = query.get(id);
	}

	@AfterEach
	void tearDown()
	{
		server.stop();
	}

	@Test
	public void serverListens()
	{
		doCheckIn(device.id, 0, LocalDateTime.now(), ""); // Throws in case of time-out
	}

	@Test
	void missingParameter_returns400() throws URISyntaxException
	{
		URI uri = new URIBuilder("http://localhost:8765/checkin").build();
		Assertions.assertEquals(400, doCheckIn(uri));
	}

	@Test
	void unknownDevice_returns400()
	{
		int status = doCheckIn(-1, 100, LocalDateTime.now(), "bla");
		Assertions.assertEquals(400, status);
	}

	@Test
	void timeThatIsOff_returns422()
	{
		int status = doCheckIn(device.id, 100, LocalDateTime.now().plusSeconds(15), "bla");
		Assertions.assertEquals(422, status);
	}

	@Test
	void incorrectHash_returns401()
	{
		int status = doCheckIn(device.id, 100, LocalDateTime.now(), "bla");
		Assertions.assertEquals(401, status);
	}

	@Test
	public void checkIn_storesBattery()
	{
		LocalDateTime time = LocalDateTime.now();
		String hash = CheckInHandler.calculateHash(device.id, time, 100, device.secretKey);
		doCheckIn(device.id, 100, time, hash);

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

	@Test
	public void calculateHashTest()
	{
		// The hashed message should be "18 2021-02-27T23:01:59 540"
		Assertions.assertEquals(
				"7a8d58cf21ba43cb0dbc63ae8413f1a70423bf7fce8b71015a9442eee8ca5672",
				CheckInHandler.calculateHash(18L, LocalDateTime.of(2021, 2, 27, 23, 1, 59), 540, "secret")
		);
	}

	private int doCheckIn(long deviceId, int battery, LocalDateTime time, String hash)
	{
		try
		{
			URI uri = new URIBuilder("http://localhost:8765/checkin")
					.addParameter("device", Long.toString(deviceId))
					.addParameter("time", time.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
					.addParameter("battery", Integer.toString(battery))
					.addParameter("hash", hash)
					.build();

			return doCheckIn(uri);
		}
		catch (URISyntaxException e)
		{
			throw new RuntimeException(e);
		}
	}

	private int doCheckIn(URI uri)
	{
		try
		{
			var request = HttpRequest.newBuilder(uri).build();

			return HttpClient.newHttpClient()
					.send(request, HttpResponse.BodyHandlers.ofString())
					.statusCode();
		}
		catch (InterruptedException | IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
