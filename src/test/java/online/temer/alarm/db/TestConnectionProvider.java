package online.temer.alarm.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TestConnectionProvider implements ConnectionProvider
{
	private static final String URL = "jdbc:mariadb://localhost/alarm_clock_test";
	private static final String USER = "test";
	private static final String PASS = "lekcmxl2ei08sx4jalcmfuryfm";

	private static Connection connection;

	@Override
	public synchronized Connection get()
	{
		if (connection == null)
		{
			try
			{
				connection = DriverManager.getConnection(URL, USER, PASS);
				connection.setAutoCommit(false);
			}
			catch (SQLException e)
			{
				throw new RuntimeException(e);
			}
		}

		return connection;
	}

	public void close()
	{
		try
		{
			if (connection != null)
			{
				connection.close();
				connection = null;
			}
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}
}
