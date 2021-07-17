package online.temer.alarm.server.handlers;

import online.temer.alarm.db.TestConnectionProvider;
import online.temer.alarm.dto.DeviceCheckInDto;
import online.temer.alarm.dto.DeviceCheckInQuery;
import online.temer.alarm.dto.DeviceDto;
import online.temer.alarm.dto.DeviceQuery;
import online.temer.alarm.dto.UserDto;
import online.temer.alarm.server.HttpUtil;
import online.temer.alarm.server.ServerTestExtension;
import online.temer.alarm.server.TestUserAuthentication;
import org.assertj.core.api.Assertions;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

@ExtendWith(ServerTestExtension.class)
public class GetCheckInsTest
{
	private DeviceDto device;
	private Connection connection;
	private DeviceQuery deviceQuery;
	private DeviceCheckInQuery deviceCheckInQuery;

	@BeforeEach
	void setUp()
	{
		connection = new TestConnectionProvider().get();
		deviceQuery = new DeviceQuery();
		deviceCheckInQuery = new DeviceCheckInQuery();
	}

	@Test
	void noCheckIns_returnsEmptyList()
	{
		createUserWithDevice(TimeZone.getDefault());

		var checkIns = new JSONObject(makeGetRequest().body())
				.getJSONArray("checkIns");

		Assertions.assertThat(checkIns.length())
				.isEqualTo(0);
	}

	@Test
	void singleCheckIn_returnsIt()
	{
		createUserWithDevice(TimeZone.getDefault());

		DeviceCheckInDto expectedCheckIn = new DeviceCheckInDto(device.id, LocalDateTime.of(2020, 10, 31, 8, 20), 95);
		deviceCheckInQuery.insertUpdate(connection, expectedCheckIn);

		assertCheckIn("2020-10-31 08:20", 95);
	}

	@Test
	void multipleCheckIns_newestOneIsReturned()
	{
		createUserWithDevice(TimeZone.getDefault());

		deviceCheckInQuery.insertUpdate(connection, new DeviceCheckInDto(device.id, LocalDateTime.of(2020, 10, 31, 8, 20), 95));
		deviceCheckInQuery.insertUpdate(connection, new DeviceCheckInDto(device.id, LocalDateTime.of(2020, 10, 31, 8, 25), 90));

		assertCheckIn("2020-10-31 08:25", 90);
	}

	@Test
	void timeIsReturnedInDeviceTimeZone()
	{
		TimeZone timeZone = TimeZone.getTimeZone(ZoneId.of("Asia/Kolkata"));
		createUserWithDevice(timeZone);

		LocalDateTime serverLocalTime = LocalDateTime.of(2020, 10, 31, 8, 20);
		deviceCheckInQuery.insertUpdate(connection, new DeviceCheckInDto(device.id, serverLocalTime, 95));

		var epochSecond = ZonedDateTime.of(serverLocalTime, ZoneId.systemDefault()).toEpochSecond();

		var timeString = new JSONObject(makeGetRequest().body())
				.getJSONArray("checkIns")
				.getJSONObject(0)
				.getString("time");

		var actualEpochSecond = LocalDateTime.parse(timeString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
				.atZone(timeZone.toZoneId())
				.toEpochSecond();

		Assertions.assertThat(actualEpochSecond)
				.isEqualTo(epochSecond);
	}

	private void createUserWithDevice(TimeZone timeZone)
	{
		device = deviceQuery.generateSaveAndLoadDevice(connection, timeZone, 10L);
		TestUserAuthentication.setAuthenticatedUser(new UserDto(10, "john@example.com", "hash", "salt"));
	}

	private void assertCheckIn(String epectedTime, int expectedBattery)
	{
		var checkIns = new JSONObject(makeGetRequest().body())
				.getJSONArray("checkIns");

		Assertions.assertThat(checkIns.length())
				.as("checkIns array length")
				.isEqualTo(1);

		var checkIn = checkIns.getJSONObject(0);
		Assertions.assertThat(checkIn.getString("time"))
				.as("time")
				.isEqualTo(epectedTime);

		Assertions.assertThat(checkIn.getInt("battery"))
				.as("battery")
				.isEqualTo(expectedBattery);
	}

	private HttpResponse<String> makeGetRequest()
	{
		try
		{
			return HttpUtil.makeGetRequest(new URI("http://localhost:8765/alarm"));
		}
		catch (URISyntaxException e)
		{
			throw new RuntimeException(e);
		}
	}
}
