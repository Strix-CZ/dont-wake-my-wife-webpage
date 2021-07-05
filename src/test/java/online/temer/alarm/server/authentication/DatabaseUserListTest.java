package online.temer.alarm.server.authentication;

import online.temer.alarm.db.DbTestExtension;
import online.temer.alarm.db.TestConnectionProvider;
import online.temer.alarm.dto.DeviceDto;
import online.temer.alarm.dto.DeviceQuery;
import online.temer.alarm.dto.UserQuery;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.sql.Connection;
import java.util.Base64;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@ExtendWith(DbTestExtension.class)
class DatabaseUserListTest
{
	private DatabaseUserList databaseUserList;
	private UserQuery userQuery;
	private Connection connection;
	private DeviceDto device;

	@BeforeEach
	void setUp()
	{
		connection = new TestConnectionProvider().get();
		userQuery = new UserQuery();
		DeviceQuery deviceQuery = new DeviceQuery();
		databaseUserList = new DatabaseUserList(userQuery, deviceQuery);

		device = deviceQuery.generateSaveAndLoadDevice(connection);
	}

	@Test
	void nonExistentUser_authenticationFailure()
	{
		Assertions.assertThat(authenticate("john@example.com", "bar"))
				.isEmpty();
	}

	@Test
	void salt_is64BytesLong()
	{
		String salt = databaseUserList.generateSalt();
		int saltLength = Base64.getDecoder().decode(salt).length;

		Assertions.assertThat(saltLength)
				.isEqualTo(64);
	}

	@Test
	void salt_isAlwaysDifferent()
	{
		int attempts = 1000;

		Set<String> salts = new HashSet<>(attempts);
		for (int i = 0; i < attempts; i++)
		{
			salts.add(databaseUserList.generateSalt());
		}

		Assertions.assertThat(salts.size())
				.isEqualTo(attempts);
	}

	@Test
	void testHash()
	{
		String hash = databaseUserList.getHash("password", "salt");

		Assertions.assertThat(hash)
				.isEqualTo("JZrW7V2byWXXSW+It3cpIeFoILSEhopgjy9kiIe9mU1CU1Wx2MSEDo7DcHuN+dBdAL1bNKKNDtSe74Nj0cZyFA==");
	}

	@Test
	void incorrectEmail_loginFails()
	{
		var user = databaseUserList.createUser("john@example.com", "bar");
		userQuery.insert(connection, user);

		Assertions.assertThat(authenticate("wrong@example.com", "bar"))
				.isEmpty();
	}

	@Test
	void incorrectPassword_loginFails()
	{
		var user = databaseUserList.createUser("john@example.com", "bar");
		userQuery.insert(connection, user);

		Assertions.assertThat(authenticate("john@example.com", "wrong"))
				.isEmpty();
	}

	@Test
	void correctCredentials_loginSucceeds()
	{
		var user = databaseUserList.createUser("john@example.com", "bar");
		userQuery.insert(connection, user);

		Assertions.assertThat(authenticate("john@example.com", "bar"))
				.isPresent()
				.get()
				.extracting(device -> device.id)
				.isEqualTo(device.id);
	}

	private Optional<DeviceDto> authenticate(String email, String password)
	{
		UserList.Credentials credentials = new UserList.Credentials(email, password);
		return databaseUserList.authenticate(credentials, connection);
	}
}