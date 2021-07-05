package online.temer.alarm.admin;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class UserManagementTest
{
	private Output output;

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

	private void execute(String... command)
	{
		output = new UserManagement().execute(command);
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
