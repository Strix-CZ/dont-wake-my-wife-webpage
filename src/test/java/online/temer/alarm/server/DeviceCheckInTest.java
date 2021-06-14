package online.temer.alarm.server;

import online.temer.alarm.db.DbTestExtension;
import online.temer.alarm.db.TestConnectionProvider;
import online.temer.alarm.dto.DeviceCheckInDto;
import online.temer.alarm.dto.DeviceDto;
import online.temer.alarm.test.util.TimeAssertion;
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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

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

		long id = query.insertDevice(
				DeviceDto.generateDevice(TimeZone.getTimeZone(ZoneId.of("Asia/Hong_Kong"))));
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
		doCheckIn(device.id, 0, getTimeInDeviceTimeZone(0), ""); // Throws in case of time-out
	}

	@Test
	void missingParameter_returns400() throws URISyntaxException
	{
		URI uri = new URIBuilder("http://localhost:8765/checkin").build();
		Assertions.assertEquals(400, doCheckIn(uri).statusCode());
	}

	@Test
	void unknownDevice_returns400()
	{
		int status = doCheckIn(-1, 100, getTimeInDeviceTimeZone(0), "bla").statusCode();
		Assertions.assertEquals(400, status);
	}

	@Test
	void timeThatIsOff_returns422AndCorrectTime()
	{
		TimeAssertion timeAssertion = new TimeAssertion();
		var response = doCheckIn(device.id, 100, getTimeInDeviceTimeZone(15), "bla");
		timeAssertion.untilNow();

		Assertions.assertEquals(422, response.statusCode());

		LocalDateTime sentTime = LocalDateTime.parse(response.body().trim(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
		timeAssertion.assertCurrentTimeIgnoringNanos(sentTime, device.timeZone);
	}

	@Test
	void incorrectHash_returns401()
	{
		int status = doCheckIn(device.id, 100, getTimeInDeviceTimeZone(0), "bla").statusCode();
		Assertions.assertEquals(401, status);
	}

	@Test
	public void checkIn_storesBattery()
	{
		ZonedDateTime time = getTimeInDeviceTimeZone(0);
		String hash = CheckInHandler.calculateHash(device.id, time.toLocalDateTime(), 100, device.secretKey);

		var response = doCheckIn(device.id, 100, getTimeInDeviceTimeZone(0), hash);

		Assertions.assertEquals(200, response.statusCode());

		TimeAssertion timeAssertion = new TimeAssertion();
		var latestUpdate = new DeviceCheckInDto.Query(new TestConnectionProvider().get())
				.getLatest(device.id);
		timeAssertion.untilNow();

		Assertions.assertNotNull(latestUpdate, "The check-in was not stored");
		Assertions.assertTrue(latestUpdate.device > 0, "id was less than 0");
		Assertions.assertEquals(device.id, latestUpdate.device, "device id");
		Assertions.assertEquals(100, latestUpdate.battery, "battery");
		timeAssertion.assertCurrentTimeIgnoringNanos(latestUpdate.time);
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

	private ZonedDateTime getTimeInDeviceTimeZone(int offsetSeconds)
	{
		return ZonedDateTime
				.now(device.timeZone.toZoneId())
				.plusSeconds(offsetSeconds);
	}

	private HttpResponse<String> doCheckIn(long deviceId, int battery, ZonedDateTime time, String hash)
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

	private HttpResponse<String> doCheckIn(URI uri)
	{
		try
		{
			var request = HttpRequest.newBuilder(uri).build();

			 return HttpClient.newHttpClient()
					.send(request, HttpResponse.BodyHandlers.ofString());
		}
		catch (InterruptedException | IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
