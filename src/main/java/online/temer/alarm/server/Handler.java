package online.temer.alarm.server;

import io.undertow.server.HttpServerExchange;

import java.util.LinkedList;
import java.util.List;

public abstract class Handler
{
	public void handle(HttpServerExchange exchange)
	{
		var parameterReader = new QueryParameterReader(exchange.getQueryParameters());
		var response = handle(parameterReader);

		exchange.setStatusCode(response.getCode());
		exchange.getResponseSender().send(response.getBody());
	}

	protected abstract Response handle(QueryParameterReader parameterReader);

	protected static class Response {

		private final List<String> lines = new LinkedList<>();
		private final int code;

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
}
