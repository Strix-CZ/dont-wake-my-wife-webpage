package online.temer.alarm.server.authentication;

import online.temer.alarm.db.DbTestExtension;
import online.temer.alarm.db.TestConnectionProvider;
import online.temer.alarm.dto.DeviceDto;
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

	@BeforeEach
	void setUp()
	{
		databaseUserList = new DatabaseUserList();
	}

	@Test
	void nonExistentUser_authenticationFailure()
	{
		Assertions.assertThat(authenticate())
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

	private Optional<DeviceDto> authenticate()
	{
		UserList.Credentials credentials = new UserList.Credentials("john@example.com", "bar");
		Connection connection = new TestConnectionProvider().get();

		return databaseUserList.authenticate(credentials, connection);
	}
}