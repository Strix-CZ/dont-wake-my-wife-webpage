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
		if (command.length == 0)
		{
			return invalidCommand();
		}

		if (command[0].equals("addUser"))
		{
			return addUser(command);
		}
		else if (command[0].equals("addDevice"))
		{
			return addDevice(command);
		}
		else
		{
			return invalidCommand();
		}
	}

	private Output addUser(String[] command)
	{
		if (command.length != 2 || command[1].isEmpty())
		{
			return new Output(1, "Incorrect arguments: addUser john@example.com");
		}

		String password = generatePassword();
		userQuery.createInsertAndLoadUser(connection, command[1], password);

		return new Output(0, "password: " + password);
	}

	private Output addDevice(String[] command)
	{
		if (command.length != 2)
		{
			return new Output(1, "Incorrect arguments: addDevice owner@example.com");
		}

		return new Output(0,
				"id: 0",
				"secret: x");
	}

	public String generatePassword()
	{
		var random = new SecureRandom();
		String consonants = "bcdfghjkmnpqrstvwxz";
		String vowels = "aeiou";

		String password = "";
		int category = random.nextInt(2);
		int categorySequenceLengh = 0;
		for (int i = 0; i < 12; ++i)
		{
			if (category == 0)
				password += "" + consonants.charAt(random.nextInt(consonants.length()));
			else
				password += "" + vowels.charAt(random.nextInt(vowels.length()));

			if (categorySequenceLengh == 1 || random.nextInt(2) == 0)
			{
				category = (category + 1) % 2;
				categorySequenceLengh = 0;
			}
			else
			{
				categorySequenceLengh++;
			}
		}

		return password;
	}

	private Output invalidCommand()
	{
		return new Output(1, "Invalid command");
	}
}
