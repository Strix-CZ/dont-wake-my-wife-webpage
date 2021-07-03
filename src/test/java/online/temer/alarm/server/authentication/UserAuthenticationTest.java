package online.temer.alarm.server.authentication;

import online.temer.alarm.db.TestConnectionProvider;
import online.temer.alarm.dto.DeviceDto;
import online.temer.alarm.dto.DeviceQuery;
import online.temer.alarm.server.Handler;
import online.temer.alarm.server.QueryParameterReader;
import online.temer.alarm.server.ServerTestExtension;
import online.temer.alarm.server.TestExceptionLogger;
import online.temer.alarm.test.util.HttpUtil;
import online.temer.alarm.util.TestAuthentication;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import spark.Spark;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.sql.Connection;

@ExtendWith(ServerTestExtension.class)
class UserAuthenticationTest
{
	private DeviceQuery deviceQuery;

	@BeforeEach
	void setUp()
	{
		deviceQuery = new DeviceQuery();
		UserAuthentication userAuthentication = new UserAuthentication(deviceQuery);

		TestAuthentication.setDelegate(userAuthentication);

		Spark.get("/test", new TestHandler());
	}

	@Test
	void noAuthorizationHeader_sends401withAuthenticateHeader() throws URISyntaxException
	{
		var request = HttpRequest.newBuilder(new URI("http://localhost:8765/alarm")).build();
		var response = HttpUtil.makeRequest(request);

		Assertions.assertThat(response.statusCode())
				.as("status code")
				.isEqualTo(401);

		Assertions.assertThat(response.headers().map().get("WWW-Authenticate"))
				.as("WWW-Authenticate header")
				.isNotEmpty()
				.containsExactly("Basic realm=\"Authenticate to Alarm\"");
	}

	/*@Test
	void noDevice_authenticationFails()
	{
		Assertions.assertThat(userAuthentication.authenticate(connection, null, null, null))
				.as("Device should be present")
				.extracting(r -> r.entity)
				.isEqualTo(Optional.empty());
	}

	@Test
	void devicePresentInDb_authenticationReturnsIt()
	{
		var deviceDto = deviceQuery.generateSaveAndLoadDevice(connection);

		Assertions.assertThat(userAuthentication.authenticate(connection, null, null, null))
				.as("Device should be present")
				.extracting(r -> r.entity)
				.isEqualTo(Optional.of(deviceDto));
	}*/

	private class TestHandler extends Handler<DeviceDto>
	{
		public TestHandler()
		{
			super(new TestConnectionProvider(), new UserAuthentication(deviceQuery), new TestExceptionLogger());
		}

		@Override
		protected Response handle(DeviceDto loggedInEntity, QueryParameterReader parameterReader, String body, Connection connection)
		{
			return new Response("authenticated");
		}
	}
}