package online.temer.alarm.dto;

import online.temer.alarm.db.DbTestExtension;
import online.temer.alarm.db.TestConnectionProvider;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.sql.Connection;

@ExtendWith(DbTestExtension.class)
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
	void insertingUser_returnsId()
	{
		long id = insertUser("john@example.com");

		Assertions.assertThat(id)
				.isGreaterThan(0);
	}

	@Test
	void insertingDuplicateEmail_fails()
	{
		insertUser("john@example.com");
		Assertions.assertThatThrownBy(() -> insertUser("john@example.com"))
				.isInstanceOf(UserQuery.DuplicateEmailException.class);
	}

	@Test
	void insertingUser_getReturnsIt()
	{
		insertUser("john@example.com");
		UserDto user = userQuery.get(connection, "john@example.com");

		Assertions.assertThat(user)
				.isNotNull();

		Assertions.assertThat(user.email)
				.as("email")
				.isEqualTo("john@example.com");

		Assertions.assertThat(user.hash)
				.as("hash")
				.isEqualTo("hash");

		Assertions.assertThat(user.salt)
				.as("salt")
				.isEqualTo("salt");
	}

	private long insertUser(String email)
	{
		return userQuery.insert(connection, new UserDto(email, "hash", "salt"));
	}
}
