package online.temer.alarm.server.authentication;

import online.temer.alarm.dto.UserDto;
import online.temer.alarm.dto.UserQuery;

import java.sql.Connection;
import java.util.Optional;

public class DatabaseUserList implements UserList
{
	private final UserQuery userQuery;

	public DatabaseUserList(UserQuery userQuery)
	{
		this.userQuery = userQuery;
	}

	@Override
	public Optional<UserDto> authenticate(Credentials credentials, Connection connection)
	{
		UserDto user = userQuery.get(connection, credentials.username);
		if (user == null)
		{
			return Optional.empty();
		}

		String hash = userQuery.getHash(credentials.password, user.salt);
		if (user.hash.equals(hash))
		{
			return Optional.of(user);
		}
		else
		{
			return Optional.empty();
		}
	}

}
