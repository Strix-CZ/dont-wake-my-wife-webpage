package online.temer.alarm.db;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.sql.Connection;
import java.sql.SQLException;

public class DbTestExtension implements
		BeforeAllCallback,
		AfterAllCallback,
		AfterEachCallback
{
	private Connection connection;

	@Override
	public void beforeAll(ExtensionContext context) throws SQLException
	{
		connection = TestConnectionProvider.getConnection();
		connection.setAutoCommit(false);

		new SchemaCreator(TestConnectionProvider.getConnection())
				.createSchema();

		connection.commit();
	}

	@Override
	public void afterAll(ExtensionContext extensionContext)
	{
		TestConnectionProvider.close();
	}

	@Override
	public void afterEach(ExtensionContext extensionContext) throws SQLException
	{
		connection.rollback();
	}
}
