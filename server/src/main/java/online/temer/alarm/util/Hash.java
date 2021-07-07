package online.temer.alarm.util;

import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class Hash
{
	private String message = "";

	public Hash addToMessage(Object o)
	{
		message += o.toString();
		return this;
	}

	public String getMessage()
	{
		return message;
	}

	public String calculateHmac(String secretKey)
	{
		try
		{
			Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
			SecretKeySpec secret_key = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
			sha256_HMAC.init(secret_key);
			byte[] hash = sha256_HMAC.doFinal(message.getBytes(StandardCharsets.UTF_8));
			return new String(Hex.encodeHex(hash));
		}
		catch (NoSuchAlgorithmException | InvalidKeyException e)
		{
			throw new RuntimeException(e);
		}
	}
}
