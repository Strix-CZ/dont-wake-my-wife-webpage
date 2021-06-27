package online.temer.alarm.server.device;

import online.temer.alarm.db.TestConnectionProvider;
import online.temer.alarm.dto.AlarmDto;
import online.temer.alarm.dto.AlarmQuery;
import online.temer.alarm.dto.DeviceCheckInQuery;
import online.temer.alarm.dto.DeviceDto;
import online.temer.alarm.dto.DeviceQuery;
import online.temer.alarm.server.ServerTestExtension;
import online.temer.alarm.test.util.HttpUtil;
import online.temer.alarm.test.util.TimeAssertion;
import org.apache.http.client.utils.URIBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

@ExtendWith(ServerTestExtension.class)
public class CheckInTest
{
	private DeviceDto device;
	private Connection connection;

	@BeforeEach
	void setUp()
	{
		connection = new TestConnectionProvider().get();
		device = new DeviceQuery().generateSaveAndLoadDevice(connection, TimeZone.getTimeZone(ZoneId.of("Asia/Hong_Kong")));
	}

	@Test
	public void serverListens()
	{
		makeGetRequest(device.id, 0, getTimeInDeviceTimeZone(0), ""); // Throws in case of time-out
	}

	@Test
	void missingParameter_returns400() throws URISyntaxException
	{
		URI uri = new URIBuilder("http://localhost:8765/checkin").build();
		Assertions.assertEquals(400, HttpUtil.makeGetRequest(uri).statusCode());
	}

	@Test
	public void checkIn_storesBattery()
	{
		TimeAssertion timeAssertion = new TimeAssertion();
		HttpResponse<String> response = makeGetRequest();
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
		var response = makeGetRequest();
		Assertions.assertEquals(200, response.statusCode(), "response code was not 200");
		Assertions.assertEquals("none", getLine(response.body(), 1), "the last line should say none");
	}

	@Test
	public void alarmSet_checkInSendsIt()
	{
		new AlarmQuery()
				.insertOrUpdateAlarm(connection, new AlarmDto(device.id, LocalTime.of(23, 6, 0)));
		var response = makeGetRequest();

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

	private HttpResponse<String> makeGetRequest()
	{
		ZonedDateTime time = getTimeInDeviceTimeZone(0);
		String hash = DeviceAuthentication.calculateHash(device.id, time.toLocalDateTime(), device.secretKey);

		return makeGetRequest(device.id, 100, time, hash);
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

	private HttpResponse<String> makeGetRequest(long deviceId, int battery, ZonedDateTime time, String hash)
	{
		try
		{
			URI uri = new URIBuilder("http://localhost:8765/checkin")
					.addParameter("device", Long.toString(deviceId))
					.addParameter("time", time.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
					.addParameter("battery", Integer.toString(battery))
					.addParameter("hash", hash)
					.build();

			return HttpUtil.makeGetRequest(uri);
		}
		catch (URISyntaxException e)
		{
			throw new RuntimeException(e);
		}
	}

}
