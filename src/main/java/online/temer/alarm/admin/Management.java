package online.temer.alarm.admin;

import online.temer.alarm.dto.UserQuery;

import java.security.SecureRandom;
import java.sql.Connection;

public class Management
{
	private final Connection connection;
	private final UserQuery userQuery;

	public Management(Connection connection, UserQuery userQuery)
	{
		this.connection = connection;
		this.userQuery = userQuery;
	}

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

		String password = generatePassword();
		userQuery.createInsertAndLoadUser(connection, command[1], password);

		return new Output(0, "password: " + password);
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
