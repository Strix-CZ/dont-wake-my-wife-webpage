package online.temer.alarm.db;

import online.temer.alarm.util.ResourceReader;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class SchemaCreator
{
	private final Connection connection;

	public SchemaCreator(Connection connection)
	{
		this.connection = connection;
	}

	public void createSchema()
	{
		String schemaStatements = new ResourceReader().readResourceTextFile("sql/scratch/create_tables.sql");

		for (String sql : splitMultipleSqls(schemaStatements))
		{
			try
			{
				connection.prepareStatement(sql).execute();
			}
			catch (SQLException e)
			{
				throw new RuntimeException("Error when executing SQL\n" + sql + "\n", e);
			}
		}
	}

	public List<String> splitMultipleSqls(String multipleSql)
	{
		return Arrays.asList(multipleSql.split(";"));
	}
}
