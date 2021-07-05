package online.temer.alarm.admin;

import online.temer.alarm.db.DbTestExtension;
import online.temer.alarm.db.TestConnectionProvider;
import online.temer.alarm.dto.UserQuery;
import online.temer.alarm.server.authentication.DatabaseUserList;
import online.temer.alarm.server.authentication.UserList;
import online.temer.alarm.test.util.TestUniqueness;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.sql.Connection;

@ExtendWith(DbTestExtension.class)
public class UserManagementTest
{
	private Output output;
	private Management management;
	private UserQuery userQuery;
	private Connection connection;
	private DatabaseUserList userList;

	@BeforeEach
	void setUp()
	{
		userQuery = new UserQuery();
		connection = new TestConnectionProvider().get();
		management = new Management(connection, userQuery);
		userList = new DatabaseUserList(userQuery);
	}

	@Test
	void unknownString_invalidCommand()
	{
		execute("invalid");
		assertFails("Invalid command");
	}

	@Test
	void noArguments_invalidCommand()
	{
		execute();
		assertFails("Invalid command");
	}

	@Test
	void addingUserWithoutEmail_incorrectArguments()
	{
		execute("addUser");
		assertFails("Incorrect arguments: addUser john@example.com");
	}

	@Test
	void addingUserWithExtraArguments_incorrectArguments()
	{
		execute("addUser", "bla", "ble");
		assertFails("Incorrect arguments: addUser john@example.com");
	}

	@Test
	void addingUserWithEmptyEmail_incorrectArguments()
	{
		execute("addUser", "");
		assertFails("Incorrect arguments: addUser john@example.com");
	}

	@Test
	void addingUser_success()
	{
		execute("addUser", "john@example.com");

		Assertions.assertThat(output.exitCode)
				.as("exit code")
				.isEqualTo(0);

		Assertions.assertThat(output.lines)
				.as("output")
				.first()
				.as("password")
				.matches(s -> s.matches("password: [a-z]+"));
	}

	@Test
	void user_isSavedInDb()
	{
		execute("addUser", "john@example.com");
		var user = userQuery.get(connection, "john@example.com");

		Assertions.assertThat(user)
				.as("saved user in DB")
				.isNotNull();

		Assertions.assertThat(user.email)
				.as("email")
				.isEqualTo("john@example.com");
	}

	@Test
	void user_canLogIn()
	{
		execute("addUser", "john@example.com");
		String password = output.lines.get(0).substring(10);

		var result = userList.authenticate(new UserList.Credentials("john@example.com", password), connection);
		Assertions.assertThat(result)
				.as("Login result")
				.isPresent();
	}

	@Test
	void generatingPassword_is10charactersLong()
	{
		Assertions.assertThat(management.generatePassword().length())
				.isEqualTo(12);
	}

	@Test
	void generatedPassword_isRandom()
	{
		TestUniqueness.assertUniqueness(1000, () -> management.generatePassword());
	}

	private void execute(String... command)
	{
		output = management.execute(command);
	}

	private void assertFails(String expectedMessage)
	{
		Assertions.assertThat(output.exitCode)
				.as("exit code")
				.isEqualTo(1);

		Assertions.assertThat(output.lines)
				.as("output")
				.containsExactly(expectedMessage);
	}
}
