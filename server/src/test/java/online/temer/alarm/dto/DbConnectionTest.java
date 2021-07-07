package online.temer.alarm.dto;

import online.temer.alarm.db.DbTestExtension;
import online.temer.alarm.db.TestConnectionProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.sql.Connection;
import java.sql.SQLException;

@ExtendWith(DbTestExtension.class)
public class DbConnectionTest
{
	private Connection connection;

	@BeforeEach
	public void setUp()
	{
		connection = new TestConnectionProvider().get();
	}

	@Test
	public void testConnection() throws SQLException
	{
		Assertions.assertNotNull(connection);
		Assertions.assertTrue(connection.isValid(1));
	}
}
