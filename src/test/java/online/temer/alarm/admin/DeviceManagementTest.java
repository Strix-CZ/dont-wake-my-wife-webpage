package online.temer.alarm.admin;

import online.temer.alarm.db.DbTestExtension;
import online.temer.alarm.db.TestConnectionProvider;
import online.temer.alarm.dto.UserQuery;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.sql.Connection;

@ExtendWith(DbTestExtension.class)
public class DeviceManagementTest
{
	private Output output;
	private Management management;
	private Connection connection;
	private UserQuery userQuery;

	@BeforeEach
	void setUp()
	{
		connection = new TestConnectionProvider().get();
		userQuery = new UserQuery();
		management = new Management(connection, userQuery);
	}

	@Test
	void addingDeviceWithExtraArguments_incorrectArguments()
	{
		execute("addDevice", "bla", "ble");
		assertIncorrectArguments();
	}

	@Test
	void addingDeviceWithFewArguments_incorrectArguments()
	{
		execute("addDevice");
		assertIncorrectArguments();
	}

	private void assertIncorrectArguments()
	{
		Assertions.assertThat(output.exitCode)
				.as("exit code")
				.isEqualTo(1);

		Assertions.assertThat(output.lines)
				.as("output")
				.containsExactly("Incorrect arguments: addDevice owner@example.com");
	}

	@Test
	void addingDeviceWithUnknownOwner_fails()
	{
		execute("addDevice", "unknown@example.com");

		Assertions.assertThat(output.exitCode)
				.as("exit code")
				.isEqualTo(1);

		Assertions.assertThat(output.lines)
				.as("output")
				.containsExactly("Unknown owner");
	}

	@Test
	void addingDevice_success()
	{
		userQuery.createInsertAndLoadUser(connection, "john@example.com", "bar");
		execute("addDevice", "john@example.com");

		Assertions.assertThat(output.exitCode)
				.as("exit code")
				.isEqualTo(0);

		Assertions.assertThat(output.lines)
				.as("output")
				.first()
				.as("first line")
				.matches(line -> line.matches("id: [0-9]+"));

		Assertions.assertThat(output.lines.get(1))
				.as("second line")
				.matches(line -> line.matches("secret: [^ ]+"));
	}

	private void execute(String... command)
	{
		output = management.execute(command);
	}
}
