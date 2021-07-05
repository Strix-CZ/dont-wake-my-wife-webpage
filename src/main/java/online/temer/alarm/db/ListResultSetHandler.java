package online.temer.alarm.db;

import org.apache.commons.dbutils.ResultSetHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class ListResultSetHandler<T> implements ResultSetHandler<List<T>>
{
	private final ResultSetHandler<T> rowHandler;

	public ListResultSetHandler(ResultSetHandler<T> rowHandler)
	{
		this.rowHandler = rowHandler;
	}

	@Override
	public List<T> handle(ResultSet rs) throws SQLException
	{
		var list = new LinkedList<T>();

		while (true)
		{
			var row = rowHandler.handle(rs);
			if (row != null)
			{
				list.add(row);
			}
			else
			{
				return list;
			}
		}
	}
}
