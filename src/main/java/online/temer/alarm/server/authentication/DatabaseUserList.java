package online.temer.alarm.server.authentication;

import online.temer.alarm.dto.DeviceDto;
import online.temer.alarm.dto.DeviceQuery;
import online.temer.alarm.dto.UserDto;
import online.temer.alarm.dto.UserQuery;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.Connection;
import java.util.Base64;
import java.util.Optional;

public class DatabaseUserList implements UserList
{
	private final UserQuery userQuery;

	public DatabaseUserList(UserQuery userQuery)
	{
		this.userQuery = userQuery;
	}

	@Override
	public Optional<UserDto> authenticate(Credentials credentials, Connection connection)
	{
		UserDto user = userQuery.get(connection, credentials.username);
		if (user == null)
		{
			return Optional.empty();
		}

		String hash = getHash(credentials.password, user.salt);
		if (user.hash.equals(hash))
		{
			return Optional.of(user);
		}
		else
		{
			return Optional.empty();
		}
	}

	public UserDto createUser(String email, String password)
	{
		String salt = generateSalt();
		String hash = getHash(password, salt);

		return new UserDto(email, hash, salt);
	}

	String generateSalt()
	{
		byte[] salt = new byte[64];
		new SecureRandom().nextBytes(salt);
		return Base64.getEncoder().encodeToString(salt);
	}

	public String getHash(String password, String salt)
	{
		try
		{
			KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(StandardCharsets.UTF_8), 9847, 512);
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			byte[] hash = factory.generateSecret(spec).getEncoded();
			return Base64.getEncoder().encodeToString(hash);
		}
		catch (NoSuchAlgorithmException | InvalidKeySpecException e)
		{
			throw new RuntimeException(e);
		}
	}
}
