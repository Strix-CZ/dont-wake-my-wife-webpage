package online.temer.alarm.server;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Methods;
import online.temer.alarm.db.ConnectionProvider;

import java.nio.charset.StandardCharsets;
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

	protected abstract Response handle(QueryParameterReader parameterReader, Connection connection);

	protected Response handlePost(QueryParameterReader parameterReader, String body, Connection connection)
	{
		return new Response(400);
	}

	@Override
	public void handleRequest(HttpServerExchange exchange)
	{
		try
		{
			if (exchange.getRequestMethod().equals(Methods.POST))
			{
				exchange.getRequestReceiver().receiveFullString(this::handlePostRequestInternal, StandardCharsets.UTF_8);
			}
			else
			{
				handleGetRequestInternal(exchange);
			}
		}
		catch (IncorrectRequest e)
		{
			sendResponse(exchange, e.response);
		}
	}

	private void handleGetRequestInternal(HttpServerExchange exchange)
	{
		var parameterReader = new QueryParameterReader(exchange.getQueryParameters());
		var response = handle(parameterReader, connectionProvider.get());
		sendResponse(exchange, response);
	}

	private void handlePostRequestInternal(HttpServerExchange exchange1, String body)
	{
		var parameterReader = new QueryParameterReader(exchange1.getQueryParameters());
		var response = handlePost(parameterReader, body, connectionProvider.get());
		sendResponse(exchange1, response);
	}

	private void sendResponse(HttpServerExchange exchange, Response response)
	{
		exchange.setStatusCode(response.getCode());
		exchange.getResponseSender().send(response.getBody());
	}

	public static class Response
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
