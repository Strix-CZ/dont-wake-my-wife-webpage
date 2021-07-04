package online.temer.alarm.server.handlers;

import online.temer.alarm.db.TestConnectionProvider;
import online.temer.alarm.dto.AlarmDto;
import online.temer.alarm.dto.AlarmQuery;
import online.temer.alarm.dto.DeviceDto;
import online.temer.alarm.dto.DeviceQuery;
import online.temer.alarm.server.ServerTestExtension;
import online.temer.alarm.server.HttpUtil;
import online.temer.alarm.server.TestAuthentication;
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

	@BeforeEach
	void setUp()
	{
		connection = new TestConnectionProvider().get();
		device = new DeviceQuery().generateSaveAndLoadDevice(connection);
		TestAuthentication.setAuthenticatedDevice(device);
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
		setAlarmInDatabase(22, 50);

		var responseJson = new JSONObject(getAlarmInGetRequest().body());

		Assertions.assertThat(responseJson.get("hour"))
				.as("hour")
				.isEqualTo(22);

		Assertions.assertThat(responseJson.get("minute"))
				.as("minute")
				.isEqualTo(50);
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
		var device = setAlarmInDatabase(20, 0);
		setAlarmInPostRequest(0, 0);

		var alarm = new AlarmQuery().get(connection, device.id);

		Assertions.assertThat(alarm.time)
				.as("alarm time")
				.isEqualTo(LocalTime.of(0, 0));
	}

	private DeviceDto setAlarmInDatabase(int hour, int minute)
	{
		var alarm = new AlarmDto(device.id, LocalTime.of(hour, minute));
		new AlarmQuery().insertOrUpdateAlarm(connection, alarm);
		return device;
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
}