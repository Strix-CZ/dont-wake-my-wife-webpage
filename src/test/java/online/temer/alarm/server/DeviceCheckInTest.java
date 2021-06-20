package online.temer.alarm.server;

import online.temer.alarm.db.DbTestExtension;
import online.temer.alarm.db.TestConnectionProvider;
import online.temer.alarm.dto.AlarmDto;
import online.temer.alarm.dto.DeviceCheckInQuery;
import online.temer.alarm.dto.DeviceDto;
import online.temer.alarm.dto.DeviceQuery;
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
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

@ExtendWith(DbTestExtension.class)
public class DeviceCheckInTest
{
	private Server server;
	private DeviceDto device;
	private Connection connection;

	@BeforeEach
	void setUp()
	{
		TestConnectionProvider testConnectionProvider = new TestConnectionProvider();
		connection = testConnectionProvider.get();

		server = new Server(8765, "localhost", testConnectionProvider);
		server.start();

		var query = new DeviceQuery(connection);

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

		LocalDateTime sentTime = LocalDateTime.parse(getLine(response.body(), 0), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
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
		TimeAssertion timeAssertion = new TimeAssertion();
		HttpResponse<String> response = doCheckIn();
		timeAssertion.untilNow();

		Assertions.assertEquals(200, response.statusCode());

		var latestUpdate = new DeviceCheckInQuery()
				.getLatest(connection, device.id);

		Assertions.assertNotNull(latestUpdate, "The check-in was not stored");
		Assertions.assertTrue(latestUpdate.device > 0, "id was less than 0");
		Assertions.assertEquals(device.id, latestUpdate.device, "device id");
		Assertions.assertEquals(100, latestUpdate.battery, "battery");
		timeAssertion.assertCurrentTimeIgnoringNanos(latestUpdate.time);
	}

	@Test
	public void noAlarmSet_checkInSendsNoAlarm()
    {
		var response = doCheckIn();
		Assertions.assertEquals(200, response.statusCode(), "response code was not 200");
		Assertions.assertEquals("none", getLine(response.body(), 1), "the last line should say none");
	}

	@Test
	public void alarmSet_checkInSendsIt()
	{
		new AlarmDto.Query(new TestConnectionProvider().get())
				.insertOrUpdateAlarm(new AlarmDto(device.id, LocalTime.of(23, 6, 0)));
		var response = doCheckIn();

		Assertions.assertEquals(200, response.statusCode(), "response code was not 200");
		Assertions.assertEquals("23:06:00", getLine(response.body(), 1));
	}

	@Test
	public void calculateHashTest()
	{
		// The hashed message should be "18 2021-02-27T23:01:59"
		Assertions.assertEquals(
				"8d58d7d2ca69ac0b522e0096b765ada4c155f70cfdab741f0f4ee2da7dc51576",
				DeviceAuthentication.calculateHash(18L, LocalDateTime.of(2021, 2, 27, 23, 1, 59), "secret")
		);
	}

	private HttpResponse<String> doCheckIn()
	{
		ZonedDateTime time = getTimeInDeviceTimeZone(0);
		String hash = DeviceAuthentication.calculateHash(device.id, time.toLocalDateTime(), device.secretKey);

		return doCheckIn(device.id, 100, time, hash);
	}

	private String getLine(String text, int lineNumber)
	{
		String[] lines = text.split("\n");
		if (lineNumber >= lines.length)
		{
			Assertions.fail("There was no line with number " + lineNumber + " (zero based). Full text:\n\"" + text + "\"");
		}

		return lines[lineNumber];
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
