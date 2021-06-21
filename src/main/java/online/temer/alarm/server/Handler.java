package online.temer.alarm.server;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import online.temer.alarm.db.ConnectionProvider;

import java.sql.Connection;
import java.util.LinkedList;
import java.util.List;

public abstract class Handler implements HttpHandler
{
	private final ConnectionProvider connectionProvider;

	public Handler(ConnectionProvider connectionProvider)
	{
		this.connectionProvider = connectionProvider;
	}

	@Override
	public void handleRequest(HttpServerExchange exchange)
	{
		var parameterReader = new QueryParameterReader(exchange.getQueryParameters());
		Response response;
		try
		{
			response = handle(parameterReader, connectionProvider.get());
		}
		catch (IncorrectRequest e)
		{
			response = e.response;
		}

		exchange.setStatusCode(response.getCode());
		exchange.getResponseSender().send(response.getBody());
	}

	protected abstract Response handle(QueryParameterReader parameterReader, Connection connection);

	protected static class Response
	{

		private final List<String> lines = new LinkedList<>();
		private final int code;

		public Response(int code)
		{
			this.code = code;
		}

		public Response(int code, String text)
		{
			this.code = code;
			lines.add(text);
		}

		public Response(String text)
		{
			this.code = 200;
			lines.add(text);
		}

		public void addLine(String line)
		{
			lines.add(line);
		}

		public String getBody()
		{
			return String.join("\n", lines);
		}

		public int getCode()
		{
			return code;
		}
	}
}
