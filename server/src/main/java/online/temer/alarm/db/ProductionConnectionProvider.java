package online.temer.alarm.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ProductionConnectionProvider implements ConnectionProvider
{
	private static final String URL = "jdbc:mariadb://localhost/alarm_clock";
	private static final String USER = "app";
	private static final String PASS = "RI05jHfMiPl0Kfxeuskno5cKlf";

	@Override
	public synchronized Connection get()
	{
		try
		{
			Connection connection = DriverManager.getConnection(URL, USER, PASS);
			connection.setAutoCommit(true);
			return connection;
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}
}
