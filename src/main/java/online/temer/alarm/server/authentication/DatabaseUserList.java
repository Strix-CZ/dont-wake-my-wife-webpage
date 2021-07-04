package online.temer.alarm.server.authentication;

import online.temer.alarm.dto.DeviceDto;

import java.security.SecureRandom;
import java.sql.Connection;
import java.util.Base64;
import java.util.Optional;

public class DatabaseUserList implements UserList
{
	static SecureRandom random;

	@Override
	public Optional<DeviceDto> authenticate(Credentials credentials, Connection connection)
	{
		return Optional.empty();
	}

	String generateSalt()
	{
		makeSureInstantiated();

		byte[] salt = new byte[64];
		random.nextBytes(salt);
		return Base64.getEncoder().encodeToString(salt);
	}

	synchronized private void makeSureInstantiated()
	{
		if (random == null)
		{
			random = new SecureRandom();
		}
	}
}
