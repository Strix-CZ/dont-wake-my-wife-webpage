package online.temer.alarm.server.authentication;

import online.temer.alarm.dto.DeviceDto;
import online.temer.alarm.server.Handler;
import online.temer.alarm.server.IncorrectRequest;
import online.temer.alarm.server.QueryParameterReader;
import spark.Request;
import spark.Response;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.Base64;
import java.util.Optional;

public class UserAuthentication implements Authentication<DeviceDto>
{
	private final UserList userList;

	public UserAuthentication(UserList userList)
	{
		this.userList = userList;
	}

	public Result<DeviceDto> authenticate(Connection connection, QueryParameterReader queryParameterReader, Request request, Response response)
	{
		if (!request.headers().contains("Authorization"))
		{
			return makeAuthorizationRequest(response, 401);
		}

		UserList.Credentials credentials = getCredentials(request.headers("Authorization"));
		Optional<DeviceDto> usersDevice = userList.authenticate(credentials, connection);

		if (usersDevice.isEmpty())
		{
			return makeAuthorizationRequest(response, 403);
		}
		else
		{
			return new Result<>(usersDevice.get());
		}
	}

	private UserList.Credentials getCredentials(String encodedHeader)
	{
		String encodedUsernamePasswordPair = encodedHeader.substring(6);
		String usernamePasswordPair = new String(Base64.getDecoder().decode(encodedUsernamePasswordPair), StandardCharsets.UTF_8);

		int locationOfColon = usernamePasswordPair.indexOf(":");
		if (locationOfColon < 0)
		{
			throw new IncorrectRequest(400);
		}

		String username = usernamePasswordPair.substring(0, locationOfColon);
		String password = usernamePasswordPair.substring(locationOfColon + 1);

		return new UserList.Credentials(username, password);
	}

	private Result<DeviceDto> makeAuthorizationRequest(Response response, int code)
	{
		response.header("WWW-Authenticate", "Basic realm=\"Authenticate to Alarm\", charset=\"UTF-8\"");
		return new Result<>(new Handler.Response(code));
	}
}
