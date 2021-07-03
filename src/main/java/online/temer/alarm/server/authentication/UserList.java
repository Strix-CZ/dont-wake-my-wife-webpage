package online.temer.alarm.server.authentication;

import online.temer.alarm.dto.DeviceDto;

import java.sql.Connection;
import java.util.Optional;

public interface UserList
{
	Optional<DeviceDto> authenticate(Credentials credentials, Connection connection);

	class Credentials
	{
		public final String username;
		public final String password;

		public Credentials(String username, String password)
		{
			this.username = username;
			this.password = password;
		}
	}
}
