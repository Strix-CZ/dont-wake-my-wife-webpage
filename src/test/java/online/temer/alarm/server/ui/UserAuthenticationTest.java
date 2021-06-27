package online.temer.alarm.server.ui;

import online.temer.alarm.db.DbTestExtension;
import online.temer.alarm.db.TestConnectionProvider;
import online.temer.alarm.dto.DeviceQuery;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.sql.Connection;

@ExtendWith(DbTestExtension.class)
class UserAuthenticationTest
{
	@Test
	void name()
	{
		Connection connection = new TestConnectionProvider().get();
		var deviceQuery = new DeviceQuery();
		deviceQuery.generateSaveAndLoadDevice(connection);

		var device = new UserAuthentication(deviceQuery).authenticate(connection);
		Assertions.assertThat(device)
				.as("authenticated with device")
				.isNotNull();
	}
}