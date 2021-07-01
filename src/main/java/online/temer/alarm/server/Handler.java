package online.temer.alarm.server;

import online.temer.alarm.db.ConnectionProvider;
import online.temer.alarm.server.authentication.Authentication;
import spark.Request;
import spark.Route;

import java.sql.Connection;
import java.util.LinkedList;
import java.util.List;

public abstract class Handler<E> implements Route
{
	private final ConnectionProvider connectionProvider;
	private final Authentication<E> authentication;

	public Handler(ConnectionProvider connectionProvider, Authentication<E> authentication)
	{
		this.connectionProvider = connectionProvider;
		this.authentication = authentication;
	}

	protected abstract Response handle(E loggedInEntity, QueryParameterReader parameterReader, String body, Connection connection);

	@Override
	public Object handle(Request request, spark.Response response)
	{
		try
		{
			var parameterReader = new QueryParameterReader(request);
			var connection = connectionProvider.get();
			final Handler.Response handlerResponse;

			var authenticationResult = authentication.authenticate(connection, parameterReader, request, response);
			if (authenticationResult.entity.isEmpty())
			{
				response.status(authenticationResult.errorResponse.getCode());
				return authenticationResult.errorResponse.getBody();
			}
			
			handlerResponse = handle(authenticationResult.entity.get(), parameterReader, request.body(), connection);

			response.status(handlerResponse.getCode());
			return handlerResponse.getBody();
		}
		catch (IncorrectRequest e)
		{
			response.status(e.response.getCode());
			return e.response.getBody();
		}
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
