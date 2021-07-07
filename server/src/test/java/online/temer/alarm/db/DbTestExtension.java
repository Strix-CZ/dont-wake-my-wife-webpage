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
	private TestConnectionProvider testConnectionProvider;
	private Connection connection;

	@Override
	public void beforeAll(ExtensionContext context)
	{
		testConnectionProvider = new TestConnectionProvider();
		connection = testConnectionProvider.get();

		new SchemaCreator(connection)
				.createSchema();
	}

	@Override
	public void afterAll(ExtensionContext extensionContext)
	{
		testConnectionProvider.close();
	}

	@Override
	public void afterEach(ExtensionContext extensionContext) throws SQLException
	{
		connection.rollback();
	}
}
