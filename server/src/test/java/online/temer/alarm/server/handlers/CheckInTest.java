package online.temer.alarm.server.handlers;

import online.temer.alarm.db.TestConnectionProvider;
import online.temer.alarm.dto.AlarmDto;
import online.temer.alarm.dto.AlarmQuery;
import online.temer.alarm.dto.DeviceCheckInQuery;
import online.temer.alarm.dto.DeviceDto;
import online.temer.alarm.dto.DeviceQuery;
import online.temer.alarm.server.ServerTestExtension;
import online.temer.alarm.server.HttpUtil;
import online.temer.alarm.test.util.TimeAssertion;
import online.temer.alarm.server.TestDeviceAuthentication;
import org.apache.http.client.utils.URIBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.time.LocalTime;
import java.time.ZoneId;
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
		device = new DeviceQuery().generateSaveAndLoadDevice(connection, TimeZone.getTimeZone(ZoneId.of("Asia/Hong_Kong")), null);
		TestDeviceAuthentication.setAuthenticatedDevice(device);
	}

	@Test
	public void serverListens()
	{
		makeGetRequest(0); // Throws in case of time-out
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
		HttpResponse<String> response = makeGetRequest(100);
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
		var response = makeGetRequest(100);
		Assertions.assertEquals(200, response.statusCode(), "response code was not 200");
		Assertions.assertEquals("none", getLine(response.body(), 1), "the last line should say none");
	}

	@Test
	public void alarmSet_checkInSendsIt()
	{
		new AlarmQuery()
				.insertOrUpdate(connection, new AlarmDto(device.id, true, LocalTime.of(23, 6, 0)));
		var response = makeGetRequest(100);

		Assertions.assertEquals(200, response.statusCode(), "response code was not 200");
		Assertions.assertEquals("23:06:00", getLine(response.body(), 1));
	}

	@Test
	public void inactiveAlarmSet_checkInSendsNoAlarm()
	{
		new AlarmQuery()
				.insertOrUpdate(connection, new AlarmDto(device.id, false, LocalTime.of(23, 6, 0)));
		var response = makeGetRequest(100);

		Assertions.assertEquals("none", getLine(response.body(), 1));
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

	private HttpResponse<String> makeGetRequest(int battery)
	{
		try
		{
			URI uri = new URIBuilder("http://localhost:8765/checkin")
					.addParameter("battery", Integer.toString(battery))
					.build();

			return HttpUtil.makeGetRequest(uri);
		}
		catch (URISyntaxException e)
		{
			throw new RuntimeException(e);
		}
	}

}
