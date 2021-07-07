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
				.isEqualTo("{}");
	}

	@Test
	void alarmIsSet_returnsIt()
	{
		setAlarmInDatabase(device, 22, 50);
		assertTimeOfAlarm(getAlarmInGetRequest(), 22, 50);
	}

	@Test
	void noAlarmIsSet_postSetsIt()
	{
		setAlarmInPostRequest(4, 0);

		var alarm = new AlarmQuery().get(connection, device.id);
		Assertions.assertThat(alarm.time)
				.as("alarm time")
				.isEqualTo(LocalTime.of(4, 0));
	}

	@Test
	void alarmIsSet_postUpdatesIt()
	{
		setAlarmInDatabase(device, 20, 0);
		setAlarmInPostRequest(0, 0);

		var alarm = new AlarmQuery().get(connection, device.id);

		Assertions.assertThat(alarm.time)
				.as("alarm time")
				.isEqualTo(LocalTime.of(0, 0));
	}

	@Test
	void multipleUsers_getsCorrectAlarm()
	{
		var otherDevice = deviceQuery.generateSaveAndLoadDevice(connection, 10);
		setAlarmInDatabase(otherDevice, 8, 59);
		setAlarmInDatabase(device, 20, 10);

		assertTimeOfAlarm(getAlarmInGetRequest(), 20, 10);
	}

	@Test
	void multipleUsers_setsCorrectAlarm()
	{
		var otherDevice = deviceQuery.generateSaveAndLoadDevice(connection, 10);
		setAlarmInDatabase(otherDevice, 8, 59);
		setAlarmInPostRequest(20, 10);

		assertTimeOfAlarm(getAlarmInGetRequest(), 20, 10);
	}

	private void setAlarmInDatabase(DeviceDto device, int hour, int minute)
	{
		var alarm = new AlarmDto(device.id, LocalTime.of(hour, minute));
		new AlarmQuery().insertOrUpdateAlarm(connection, alarm);
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

	private HttpResponse<String> setAlarmInPostRequest(int hour, int minute)
	{
		try
		{
			var body = new JSONObject()
					.put("hour", hour)
					.put("minute", minute);

			return HttpUtil.makePostResquest(new URI("http://localhost:8765/alarm"), body.toString());
		}
		catch (URISyntaxException e)
		{
			throw new RuntimeException(e);
		}
	}

	private void assertTimeOfAlarm(HttpResponse<String> response, int hour, int minute)
	{
		Assertions.assertThat(response.statusCode())
				.as("HTTP status code")
				.isEqualTo(200);

		var responseJson = new JSONObject(response.body());

		Assertions.assertThat(responseJson.get("hour"))
				.as("hour")
				.isEqualTo(hour);

		Assertions.assertThat(responseJson.get("minute"))
				.as("minute")
				.isEqualTo(minute);
	}
}