package online.temer.alarm.server.authentication;

import online.temer.alarm.db.DbTestExtension;
import online.temer.alarm.db.TestConnectionProvider;
import online.temer.alarm.dto.UserDto;
import online.temer.alarm.dto.UserQuery;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.sql.Connection;
import java.util.Optional;

@ExtendWith(DbTestExtension.class)
class DatabaseUserListTest
{
	private DatabaseUserList databaseUserList;
	private UserQuery userQuery;
	private Connection connection;

	@BeforeEach
	void setUp()
	{
		connection = new TestConnectionProvider().get();
		userQuery = new UserQuery();
		databaseUserList = new DatabaseUserList(userQuery);
	}

	@Test
	void nonExistentUser_authenticationFailure()
	{
		Assertions.assertThat(authenticate("john@example.com", "bar"))
				.isEmpty();
	}

	@Test
	void incorrectEmail_loginFails()
	{
		userQuery.createInsertAndLoadUser(connection, "john@example.com", "bar");

		Assertions.assertThat(authenticate("wrong@example.com", "bar"))
				.isEmpty();
	}

	@Test
	void incorrectPassword_loginFails()
	{
		userQuery.createInsertAndLoadUser(connection, "john@example.com", "bar");

		Assertions.assertThat(authenticate("john@example.com", "wrong"))
				.isEmpty();
	}

	@Test
	void correctCredentials_loginSucceeds()
	{
		var user = userQuery.createInsertAndLoadUser(connection, "john@example.com", "bar");

		Assertions.assertThat(authenticate("john@example.com", "bar"))
				.isPresent()
				.get()
				.isEqualTo(user);
	}

	private Optional<UserDto> authenticate(String email, String password)
	{
		UserList.Credentials credentials = new UserList.Credentials(email, password);
		return databaseUserList.authenticate(credentials, connection);
	}
}