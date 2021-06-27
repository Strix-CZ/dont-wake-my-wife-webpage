package online.temer.alarm.server.ui;

import online.temer.alarm.dto.DeviceDto;
import online.temer.alarm.dto.DeviceQuery;

import java.sql.Connection;

public class UserAuthentication
{
	private final DeviceQuery deviceQuery;

	public UserAuthentication(DeviceQuery deviceQuery)
	{
		this.deviceQuery = deviceQuery;
	}

	public DeviceDto authenticate(Connection connection)
	{
		// No authorization at the moment. Just return any device.
		return deviceQuery.get(connection);
	}
}
