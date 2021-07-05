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

	@BeforeEach
	void setUp()
	{
		connection = new TestConnectionProvider().get();
		management = new Management(connection, new UserQuery());
	}

	@Test
	void addingDeviceWithExtraArguments_incorrectArguments()
	{
		execute("addDevice", "bla");
		assertFails("There are no arguments for addDevice");
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
