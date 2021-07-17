package online.temer.alarm.server.handlers;

import online.temer.alarm.db.TestConnectionProvider;
import online.temer.alarm.dto.AlarmDto;
import online.temer.alarm.dto.AlarmQuery;
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
import java.time.LocalTime;

@ExtendWith(ServerTestExtension.class)
class AlarmHandlerTest
{
	private DeviceDto device;
	private Connection connection;
	private DeviceQuery deviceQuery;

	@BeforeEach
	void setUp()
	{
		connection = new TestConnectionProvider().get();
		deviceQuery = new DeviceQuery();
		device = deviceQuery.generateSaveAndLoadDevice(connection, 10);
		TestUserAuthentication.setAuthenticatedUser(new UserDto(10, "john@example.com", "hash", "salt"));
	}

	@Test
	void whenQueried_returns200()
	{
		Assertions.assertThat(getAlarmInGetRequest().statusCode())
				.as("status code")
				.isEqualTo(200);
	}

	@Test
	void noAlarmsSet_returnsEmptyObject()
	{
		Assertions.assertThat(getAlarmInGetRequest().body())
				.as("response body")
				.isEqualTo("{\"alarm\":{}}");
	}

	@Test
	void alarmIsSet_returnsIt()
	{
		setAlarmInDatabase(device, true, 22, 50);
		assertAlarm(getAlarmInGetRequest(), true, 22, 50);
	}

	@Test
	void noAlarmIsSet_postSetsIt()
	{
		setAlarmInPostRequest(false, 4, 0);

		var alarm = new AlarmQuery().get(connection, device.id);

		Assertions.assertThat(alarm.isActive)
				.as("isActive")
				.isEqualTo(false);

		Assertions.assertThat(alarm.time)
				.as("alarm time")
				.isEqualTo(LocalTime.of(4, 0));
	}

	@Test
	void alarmIsSet_postUpdatesIt()
	{
		setAlarmInDatabase(device, false, 20, 0);
		setAlarmInPostRequest(true, 0, 0);

		var alarm = new AlarmQuery().get(connection, device.id);

		Assertions.assertThat(alarm.isActive)
				.as("isActive")
				.isEqualTo(true);

		Assertions.assertThat(alarm.time)
				.as("alarm time")
				.isEqualTo(LocalTime.of(0, 0));
	}

	@Test
	void multipleUsers_getsCorrectAlarm()
	{
		var otherDevice = deviceQuery.generateSaveAndLoadDevice(connection, 10);
		setAlarmInDatabase(otherDevice, false, 8, 59);
		setAlarmInDatabase(device, true, 20, 10);

		assertAlarm(getAlarmInGetRequest(), true, 20, 10);
	}

	@Test
	void multipleUsers_setsCorrectAlarm()
	{
		var otherDevice = deviceQuery.generateSaveAndLoadDevice(connection, 10);
		setAlarmInDatabase(otherDevice, true, 8, 59);
		setAlarmInPostRequest(false, 20, 10);

		assertAlarm(getAlarmInGetRequest(), false, 20, 10);
	}

	@Test
	void sendingNull_isInvalidRequest() throws URISyntaxException
	{
		setAlarmInDatabase(device, true, 20, 0);
		var response = HttpUtil.makePostResquest(new URI("http://localhost:8765/alarm"), "null");

		Assertions.assertThat(response.statusCode())
				.as("response")
				.isEqualTo(400);
	}

	private void setAlarmInDatabase(DeviceDto device, boolean isActive, int hour, int minute)
	{
		var alarm = new AlarmDto(device.id, isActive, LocalTime.of(hour, minute));
		new AlarmQuery().insertOrUpdate(connection, alarm);
	}

	private HttpResponse<String> getAlarmInGetRequest()
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

	private HttpResponse<String> setAlarmInPostRequest(boolean isActive, int hour, int minute)
	{
		try
		{
			var body = new JSONObject()
					.put("isActive", isActive)
					.put("hour", hour)
					.put("minute", minute);

			return HttpUtil.makePostResquest(new URI("http://localhost:8765/alarm"), body.toString());
		}
		catch (URISyntaxException e)
		{
			throw new RuntimeException(e);
		}
	}

	private void assertAlarm(HttpResponse<String> response, boolean isActive, int hour, int minute)
	{
		Assertions.assertThat(response.statusCode())
				.as("HTTP status code")
				.isEqualTo(200);

		var alarmResponse = new JSONObject(response.body())
				.getJSONObject("alarm");

		Assertions.assertThat(alarmResponse.get("isActive"))
				.as("isActive")
				.isEqualTo(isActive);

		Assertions.assertThat(alarmResponse.get("hour"))
				.as("hour")
				.isEqualTo(hour);

		Assertions.assertThat(alarmResponse.get("minute"))
				.as("minute")
				.isEqualTo(minute);
	}
}