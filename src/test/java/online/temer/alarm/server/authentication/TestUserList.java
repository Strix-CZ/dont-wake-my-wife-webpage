package online.temer.alarm.server.authentication;

import online.temer.alarm.dto.DeviceDto;
import online.temer.alarm.dto.DeviceQuery;

import java.sql.Connection;
import java.util.Optional;

public class TestUserList implements UserList
{
	private final DeviceQuery deviceQuery;

	public TestUserList(DeviceQuery deviceQuery)
	{
		this.deviceQuery = deviceQuery;
	}

	@Override
	public Optional<DeviceDto> authenticate(Credentials credentials, Connection connection)
	{
		if (credentials.username.equals("testuser") && credentials.password.equals("bar"))
		{
			return Optional.ofNullable(deviceQuery.get(connection));
		}
		else
		{
			return Optional.empty();
		}
	}
}
