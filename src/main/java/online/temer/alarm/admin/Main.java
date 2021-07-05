package online.temer.alarm.admin;

import online.temer.alarm.db.ProductionConnectionProvider;
import online.temer.alarm.dto.DeviceQuery;
import online.temer.alarm.dto.UserQuery;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class Main
{
	public static void main(String[] args)
	{

		try (Connection connection = new ProductionConnectionProvider().get())
		{
			var management = new Management(connection, new UserQuery(), new DeviceQuery());

			List<String> arguments = Arrays.asList(args);
			arguments.remove(0);
			arguments.remove(0);

			management.execute(arguments.toArray(String[]::new));
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}
}
