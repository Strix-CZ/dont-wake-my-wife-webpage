package online.temer.alarm.admin;

import online.temer.alarm.db.DbTestExtension;
import online.temer.alarm.db.TestConnectionProvider;
import online.temer.alarm.dto.UserQuery;
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
	private UserManagement userManagement;
	private UserQuery userQuery;
	private Connection connection;

	@BeforeEach
	void setUp()
	{
		userQuery = new UserQuery();
		connection = new TestConnectionProvider().get();
		userManagement = new UserManagement(connection, userQuery);
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
		execute("add");
		assertFails("Incorrect arguments: add email");
	}

	@Test
	void addingUserWithExtraArguments_incorrectArguments()
	{
		execute("add", "bla", "ble");
		assertFails("Incorrect arguments: add email");
	}

	@Test
	void addingUserWithEmptyEmail_incorrectArguments()
	{
		execute("add", "");
		assertFails("Incorrect arguments: add email");
	}

	@Test
	void addingUser_success()
	{
		execute("add", "john@example.com");

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
		execute("add", "john@example.com");
		var user = userQuery.get(connection, "john@example.com");

		Assertions.assertThat(user)
				.as("saved user in DB")
				.isNotNull();

		Assertions.assertThat(user.email)
				.as("email")
				.isEqualTo("john@example.com");
	}

	@Test
	void generatingPassword_is10charactersLong()
	{
		Assertions.assertThat(userManagement.generatePassword().length())
				.isEqualTo(12);
	}

	@Test
	void generatedPassword_isRandom()
	{
		TestUniqueness.assertUniqueness(1000, () -> userManagement.generatePassword());
	}

	private void execute(String... command)
	{
		output = userManagement.execute(command);
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
