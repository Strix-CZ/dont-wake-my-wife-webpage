package online.temer.alarm.dto;

import online.temer.alarm.db.TestConnectionProvider;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;

public class UserDtoTest
{
	private Connection connection;
	private UserQuery userQuery;

	@BeforeEach
	void setUp()
	{
		connection = new TestConnectionProvider().get();
		userQuery = new UserQuery();
	}

	@Test
	void getNonexistentUser_returnsNull()
	{
		Assertions.assertThat(userQuery.get(connection, "johndoe@example.com"))
				.isNull();
	}

	@Test
	void insertingUser_getReturnsIt()
	{
		userQuery.insert(connection, new UserDto("john@example.com"));

		Assertions.assertThat(userQuery.get(connection, "john@example.com"))
				.isNotNull()
				.extracting(u -> u.email)
				.isEqualTo("john@example.com");
	}
}
