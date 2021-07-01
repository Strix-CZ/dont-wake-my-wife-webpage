package online.temer.alarm.server.ui;

import online.temer.alarm.db.DbTestExtension;
import online.temer.alarm.db.TestConnectionProvider;
import online.temer.alarm.dto.DeviceQuery;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.sql.Connection;

@ExtendWith(DbTestExtension.class)
class UserAuthenticationTest
{
	private Connection connection;
	private DeviceQuery deviceQuery;
	private UserAuthentication userAuthentication;

	@BeforeEach
	void setUp()
	{
		connection = new TestConnectionProvider().get();
		deviceQuery = new DeviceQuery();
		userAuthentication = new UserAuthentication(deviceQuery);
	}

	@Test
	void noDevice_authenticationFails()
	{
		Assertions.assertThat(userAuthentication.authenticate(connection))
				.as("Device should be present")
				.isEmpty();
	}

	@Test
	void devicePresentInDb_authenticationReturnsIt()
	{
		deviceQuery.generateSaveAndLoadDevice(connection);

		Assertions.assertThat(userAuthentication.authenticate(connection))
				.as("Device should be present")
				.isPresent();
	}
}