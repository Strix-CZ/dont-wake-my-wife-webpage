package online.temer.alarm.server;

import online.temer.alarm.dto.UserDto;
import online.temer.alarm.server.authentication.Authentication;
import spark.Request;
import spark.Response;

import java.sql.Connection;

public class TestUserAuthentication implements Authentication<UserDto>
{
	private static boolean useDelegate;
	private static Authentication<UserDto> delegate;
	private static Result<UserDto> authenticationResult;

	@Override
	public Result<UserDto> authenticate(Connection connection, QueryParameterReader parameterReader, Request request, Response response)
	{
		if (useDelegate)
		{
			return delegate.authenticate(connection, parameterReader, request, response);
		}

		if (authenticationResult == null)
		{
			throw new IllegalStateException("Authentication result was not set");
		}

		return authenticationResult;
	}

	public static void setAuthenticatedUser(UserDto userDto)
	{
		useDelegate = false;
		authenticationResult = new Result<>(userDto);
	}

	public static void setAuthenticationUndefined()
	{
		useDelegate = false;
		authenticationResult = null;
	}

	public static void setDelegate(Authentication<UserDto> authenticationDelegate)
	{
		useDelegate = true;
		delegate = authenticationDelegate;
	}
}
