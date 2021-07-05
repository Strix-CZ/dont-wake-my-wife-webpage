package online.temer.alarm.server.authentication;

import online.temer.alarm.dto.DeviceDto;

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
	static SecureRandom random;

	@Override
	public Optional<DeviceDto> authenticate(Credentials credentials, Connection connection)
	{
		return Optional.empty();
	}

	String generateSalt()
	{
		makeSureRandomIsInstantiated();

		byte[] salt = new byte[64];
		random.nextBytes(salt);
		return Base64.getEncoder().encodeToString(salt);
	}

	synchronized private void makeSureRandomIsInstantiated()
	{
		if (random == null)
		{
			random = new SecureRandom();
		}
	}

	public String getHash(String password, String salt)
	{
		try
		{
			KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(StandardCharsets.UTF_8), 49847, 512);
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
