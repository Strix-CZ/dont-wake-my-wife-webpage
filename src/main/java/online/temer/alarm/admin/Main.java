package online.temer.alarm.admin;

import online.temer.alarm.db.ProductionConnectionProvider;
import online.temer.alarm.dto.DeviceQuery;
import online.temer.alarm.dto.UserQuery;

import java.sql.Connection;
import java.sql.SQLException;

public class Main
{
	public static void main(String[] args)
	{

		try (Connection connection = new ProductionConnectionProvider().get())
		{
			var management = new Management(connection, new UserQuery(), new DeviceQuery());

			Output output = management.execute(args);

			System.out.println(String.join("\n", output.lines));
			System.exit(output.exitCode);
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}
}
