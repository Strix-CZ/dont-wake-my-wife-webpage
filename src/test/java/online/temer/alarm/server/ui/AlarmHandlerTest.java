package online.temer.alarm.server.ui;

import online.temer.alarm.db.TestConnectionProvider;
import online.temer.alarm.dto.AlarmDto;
import online.temer.alarm.dto.AlarmQuery;
import online.temer.alarm.dto.DeviceQuery;
import online.temer.alarm.server.ServerTestExtension;
import online.temer.alarm.test.util.HttpUtil;
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
	private DeviceQuery deviceQuery;
	private Connection connection;

	@BeforeEach
	void setUp()
	{
		connection = new TestConnectionProvider().get();
		deviceQuery = new DeviceQuery();
	}

	@Test
	void noDevice_returns401()
	{
		Assertions.assertThat(getAlarm().statusCode())
				.as("status code")
				.isEqualTo(401);
	}

	@Test
	void whenQueried_returns200()
	{
		deviceQuery.generateSaveAndLoadDevice(connection);

		Assertions.assertThat(getAlarm().statusCode())
				.as("status code")
				.isEqualTo(200);
	}

	@Test
	void noAlarmsSet_returnsEmptyObject()
	{
		deviceQuery.generateSaveAndLoadDevice(connection);

		Assertions.assertThat(getAlarm().body())
				.as("response body")
				.isEqualTo("{}");
	}

	@Test
	void alarmIsSet_returnsIt()
	{
		createDeviceAndSetAlarm(22, 50);

		var responseJson = new JSONObject(getAlarm().body());

		Assertions.assertThat(responseJson.get("hour"))
				.as("hour")
				.isEqualTo(22);

		Assertions.assertThat(responseJson.get("minute"))
				.as("minute")
				.isEqualTo(50);
	}

	private void createDeviceAndSetAlarm(int hour, int minute)
	{
		var device = deviceQuery.generateSaveAndLoadDevice(connection);
		var alarm = new AlarmDto(device.id, LocalTime.of(hour, minute));
		new AlarmQuery().insertOrUpdateAlarm(connection, alarm);
	}

	private HttpResponse<String> getAlarm()
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