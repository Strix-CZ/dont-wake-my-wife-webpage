package online.temer.alarm.server;

import online.temer.alarm.dto.DeviceDto;
import online.temer.alarm.server.authentication.Authentication;
import spark.Request;
import spark.Response;

import java.sql.Connection;

public class TestDeviceAuthentication implements Authentication<DeviceDto>
{
	private static boolean useDelegate;
	private static Authentication<DeviceDto> delegate;
	private static Authentication.Result<DeviceDto> authenticationResult;

	@Override
	public Result<DeviceDto> authenticate(Connection connection, QueryParameterReader parameterReader, Request request, Response response)
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

	public static void setAuthenticatedDevice(DeviceDto deviceDto)
	{
		useDelegate = false;
		authenticationResult = new Authentication.Result<>(deviceDto);
	}

	public static void setAuthenticationUndefined()
	{
		useDelegate = false;
		authenticationResult = null;
	}
}
