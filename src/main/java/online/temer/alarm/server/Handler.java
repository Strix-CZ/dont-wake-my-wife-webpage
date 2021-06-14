package online.temer.alarm.server;

import io.undertow.server.HttpServerExchange;

import java.util.LinkedList;
import java.util.List;

public abstract class Handler
{
	public void handle(HttpServerExchange exchange)
	{
		Response response;

		try {
			var parameterReader = new QueryParameterReader(exchange.getQueryParameters());
			response = handle(parameterReader);
		} catch (ValidationException e) {
			response = e.response;
		}

		exchange.setStatusCode(response.code);
		exchange.getResponseSender().send(response.getBody());
	}

	protected abstract Response handle(QueryParameterReader parameterReader);

	protected static class Response {

		public final List<String> lines = new LinkedList<>();
		public final int code;

		public Response(int code) {
			this.code = code;
		}

		public Response(int code, String text) {
			this.code = code;
			lines.add(text);
		}

		public Response(String text) {
			this.code = 200;
			lines.add(text);
		}

		public void addLine(String line) {
			lines.add(line);
		}

		public String getBody() {
			return String.join("\n", lines);
		}

		public int getCode() {
			return code;
		}

	}

	protected static class ValidationException extends IllegalArgumentException {
		public final Response response;

		public ValidationException(int code) {
			super();
			this.response = new Response(code);
		}

		public ValidationException(int code, String message) {
			super(message);
			this.response = new Response(code, message);
		}
	}
}
