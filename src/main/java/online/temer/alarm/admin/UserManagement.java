package online.temer.alarm.admin;

public class UserManagement
{
	public Output execute(String... command)
	{
		if (command.length > 0 && command[0].equals("add"))
		{
			return new Output(1, "Incorrect arguments: add email");
		}
		else
		{
			return new Output(1, "Invalid command");
		}
	}
}
