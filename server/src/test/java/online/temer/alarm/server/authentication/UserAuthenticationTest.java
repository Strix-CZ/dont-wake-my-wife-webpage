package online.temer.alarm.server.authentication;

import online.temer.alarm.db.TestConnectionProvider;
import online.temer.alarm.dto.UserDto;
import online.temer.alarm.server.Handler;
import online.temer.alarm.server.HttpUtil;
import online.temer.alarm.server.QueryParameterReader;
import online.temer.alarm.server.ServerTestExtension;
import online.temer.alarm.server.TestExceptionLogger;
import online.temer.alarm.server.TestUserAuthentication;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import spark.Spark;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.util.Optional;

@ExtendWith(ServerTestExtension.class)
class UserAuthenticationTest
{
	@BeforeEach
	void setUp()
	{
		UserAuthentication userAuthentication = new UserAuthentication(new TestUserList());

		TestUserAuthentication.setDelegate(userAuthentication);

		Spark.get("/test", new TestHandler());
	}

	@Test
	void noAuthorizationHeader_sends401withAuthenticateHeader() throws URISyntaxException
	{
		var request = HttpRequest.newBuilder(new URI("http://localhost:8765/alarm")).build();
		var response = HttpUtil.makeRequest(request);

		assertAuthenticationFailure(response, 401);
	}

	@Test
	void garbageInsteadOfBase64_sends401() throws URISyntaxException
	{
		var response = makeRequest("garbage");

		Assertions.assertThat(response.statusCode())
				.as("status code")
				.isEqualTo(400);
	}

	@Test
	void noColonInRequest_sends401() throws URISyntaxException
	{
		var response = makeRequest("am9obmRvZWJhcg=="); // johndoebar

		Assertions.assertThat(response.statusCode())
				.as("status code")
				.isEqualTo(400);
	}

	@Test
	void wrongUsername_sends403withAuthenticateHeader() throws URISyntaxException
	{
		var response = makeRequest("am9obmRvZTpiYXI="); // johndoe:bar
		assertAuthenticationFailure(response, 403);
	}

	@Test
	void wrongPassword_sends403withAuthenticateHeader() throws URISyntaxException
	{
		var response = makeRequest("dGVzdHVzZXI6YmxhaA=="); // testuser:blah
		assertAuthenticationFailure(response, 403);
	}

	@Test
	void correctRequest_deviceIsReturned() throws URISyntaxException
	{
		var response = makeRequest("dGVzdHVzZXI6YmFy"); // testuser:bar

		Assertions.assertThat(response.statusCode())
				.as("status code")
				.isEqualTo(200);

		Assertions.assertThat(response.body())
				.as("body was not sent")
				.isEqualTo("OK");
	}

	private void assertAuthenticationFailure(HttpResponse<String> response, int expectedStatusCode)
	{
		Assertions.assertThat(response.statusCode())
				.as("status code")
				.isEqualTo(expectedStatusCode);

		Assertions.assertThat(response.headers().map().get("WWW-Authenticate"))
				.as("WWW-Authenticate header")
				.isNotEmpty()
				.containsExactly("Basic realm=\"Authenticate to Alarm\", charset=\"UTF-8\"");
	}

	private HttpResponse<String> makeRequest(String encodedUsernamePassword) throws URISyntaxException
	{
		var request = HttpRequest.newBuilder(new URI("http://localhost:8765/test"))
				.header("Authorization", "Basic " + encodedUsernamePassword)
				.build();
		return HttpUtil.makeRequest(request);
	}

	private static class TestHandler extends Handler<UserDto>
	{
		public TestHandler()
		{
			super(new TestConnectionProvider(), new UserAuthentication(new TestUserList()), new TestExceptionLogger());
		}

		@Override
		protected Response handle(UserDto entity, QueryParameterReader parameterReader, String body, Connection connection)
		{
			return new Response("OK");
		}
	}

	public static class TestUserList implements UserList
	{
		@Override
		public Optional<UserDto> authenticate(Credentials credentials, Connection connection)
		{
			if (credentials.username.equals("testuser") && credentials.password.equals("bar"))
			{
				return Optional.of(new UserDto("testuser", "hash", "salt"));
			}
			else
			{
				return Optional.empty();
			}
		}
	}
}