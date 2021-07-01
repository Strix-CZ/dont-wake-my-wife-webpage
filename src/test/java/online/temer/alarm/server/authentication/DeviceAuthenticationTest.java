package online.temer.alarm.server.authentication;

import online.temer.alarm.db.DbTestExtension;
import online.temer.alarm.db.TestConnectionProvider;
import online.temer.alarm.dto.DeviceDto;
import online.temer.alarm.dto.DeviceQuery;
import online.temer.alarm.server.Handler;
import online.temer.alarm.server.IncorrectParameter;
import online.temer.alarm.server.QueryParameterReader;
import online.temer.alarm.test.util.TimeAssertion;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

@ExtendWith(DbTestExtension.class)
class DeviceAuthenticationTest
{
	private DeviceAuthentication deviceAuthentication;
	private Connection connection;
	private DeviceDto device;

	@BeforeEach
	public void setUp()
	{
		connection = new TestConnectionProvider().get();
		DeviceQuery deviceQuery = new DeviceQuery();
		deviceAuthentication = new DeviceAuthentication(deviceQuery);

		device = deviceQuery.generateSaveAndLoadDevice(connection, TimeZone.getTimeZone(ZoneId.of("Asia/Hong_Kong")));
	}

	@Test
	void missingDeviceParameter_throws400()
	{
		var parameters = new QueryParameterReader(
				"time", getFormattedTimeInDeviceTimeZone(0),
				"hash", "incorrect");

		assertAuthenticationThrowsIncorrectParameter(parameters);
	}

	@Test
	void missingTimeParameter_throws400()
	{
		var parameters = new QueryParameterReader(
				"device", device.id.toString(),
				"hash", "incorrect");

		assertAuthenticationThrowsIncorrectParameter(parameters);
	}

	@Test
	void missingHashParameter_throws400()
	{
		var parameters = new QueryParameterReader(
				"device", device.id.toString(),
				"time", getFormattedTimeInDeviceTimeZone(0));

		assertAuthenticationThrowsIncorrectParameter(parameters);
	}

	@Test
	void unknownDevice_throws400()
	{
		var parameters = new QueryParameterReader(
				"device", "-1",
				"time", getFormattedTimeInDeviceTimeZone(0),
				"hash", "incorrect");

		assertAuthenticationFails(parameters, 400);
	}

	@Test
	void incorrectHash_throws401()
	{
		var parameters = new QueryParameterReader(
				"device", device.id.toString(),
				"time", getFormattedTimeInDeviceTimeZone(0),
				"hash", "incorrect");

		assertAuthenticationFails(parameters, 401);
	}

	@Test
	void timeThatIsOff_returns422()
	{
		var parameters = new QueryParameterReader(
				"device", device.id.toString(),
				"time", getFormattedTimeInDeviceTimeZone(50),
				"hash", "incorrect");

		assertAuthenticationFails(parameters, 422);
	}

	@Test
	void timeThatIsOff_returnsCorrectTime()
	{
		var parameters = new QueryParameterReader(
				"device", device.id.toString(),
				"time", getFormattedTimeInDeviceTimeZone(50),
				"hash", "incorrect");

		var timeAssertion = new TimeAssertion();
		var responseBody = deviceAuthentication.authenticate(connection, parameters, null, null).errorResponse.getBody();
		timeAssertion.untilNow();

		LocalDateTime sentTime = LocalDateTime.parse(responseBody.trim(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
		timeAssertion.assertCurrentTimeIgnoringNanos(sentTime, device.timeZone);
	}

	@Test
	void correctRequest_returnsTheDevice()
	{
		var time = getFormattedTimeInDeviceTimeZone(0);
		var hash = DeviceAuthentication.calculateHash(device.id, LocalDateTime.parse(time, DateTimeFormatter.ISO_LOCAL_DATE_TIME), device.secretKey);
		var parameters = new QueryParameterReader(
				"device", device.id.toString(),
				"time", time,
				"hash", hash);

		var authenticatedDevice = deviceAuthentication.authenticate(connection, parameters, null, null).getEntity();

		Assertions.assertThat(authenticatedDevice)
				.as("authenticated device")
				.isPresent()
				.get()
				.isEqualTo(device);
	}

	@Test
	public void calculateHashTest()
	{
		// The hashed message should be "18 2021-02-27T23:01:59"
		org.junit.jupiter.api.Assertions.assertEquals(
				"8d58d7d2ca69ac0b522e0096b765ada4c155f70cfdab741f0f4ee2da7dc51576",
				DeviceAuthentication.calculateHash(18L, LocalDateTime.of(2021, 2, 27, 23, 1, 59), "secret")
		);
	}

	private void assertAuthenticationThrowsIncorrectParameter(QueryParameterReader parameters)
	{
		Assertions.assertThatThrownBy(() -> deviceAuthentication.authenticate(connection, parameters, null, null))
				.isInstanceOf(IncorrectParameter.class);
	}

	private void assertAuthenticationFails(QueryParameterReader parameters, int expectedStatusCode)
	{
		Assertions.assertThat(deviceAuthentication.authenticate(connection, parameters, null, null).errorResponse)
				.as("error response")
				.isNotNull()
				.extracting(Handler.Response::getCode)
				.isEqualTo(expectedStatusCode);
	}

	private String getFormattedTimeInDeviceTimeZone(int offsetSeconds)
	{
		return ZonedDateTime
				.now(device.timeZone.toZoneId())
				.plusSeconds(offsetSeconds)
				.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
	}
}