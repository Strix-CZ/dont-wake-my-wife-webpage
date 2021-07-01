package online.temer.alarm.util;

import online.temer.alarm.dto.DeviceDto;
import online.temer.alarm.server.Handler;
import online.temer.alarm.server.QueryParameterReader;
import online.temer.alarm.server.authentication.Authentication;
import spark.Request;
import spark.Response;

import java.sql.Connection;

public class TestAuthentication implements Authentication<DeviceDto>
{
	private static Authentication.Result<DeviceDto> authenticationResult;

	@Override
	public Result<DeviceDto> authenticate(Connection connection, QueryParameterReader parameterReader, Request request, Response response)
	{
		if (authenticationResult == null)
		{
			throw new IllegalStateException("Authentication result was not set");
		}

		return authenticationResult;
	}

	public static void setAuthenticatedDevice(DeviceDto deviceDto)
	{
		authenticationResult = new Authentication.Result<>(deviceDto);
	}

	public static void setAuthenticationError(Handler.Response errorResponse)
	{
		authenticationResult = new Authentication.Result<>(errorResponse);
	}

	public static void setAuthenticationUndefined()
	{
		authenticationResult = null;
	}
}
