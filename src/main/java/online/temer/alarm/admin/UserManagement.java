package online.temer.alarm.admin;

import java.security.SecureRandom;
import java.util.Base64;

public class UserManagement
{
	public Output execute(String... command)
	{
		if (command.length == 0 || !command[0].equals("add"))
		{
			return new Output(1, "Invalid command");
		}

		if (command.length != 2 || command[1].isEmpty())
		{
			return new Output(1, "Incorrect arguments: add email");
		}

		return new Output(0, "password: " + generatePassword());
	}

	public String generatePassword()
	{
		var random = new SecureRandom();
		String consonants = "bcdfghjklmnpqrstvwxyz";
		String vowels = "aeiouy";

		String password = "";
		for (int i = 0; i < 6; ++i)
		{
			password += "" + consonants.charAt(random.nextInt(consonants.length()))
					  + vowels.charAt(random.nextInt(vowels.length()));
		}

		return password;
	}
}
