package online.temer.alarm.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HashTest
{
	@Test
	public void testMessageHash()
	{
		String hash = new Hash()
				.addToMessage("The quick brown fox jumps over the lazy dog")
				.calculateHmac("key");

		assertEquals("f7bc83f430538424b13298e6aa6fb143ef4d59a14946175997479dbc2d1a3cd8", hash);
	}
}