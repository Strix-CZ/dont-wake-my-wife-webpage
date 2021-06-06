package online.temer.alarm.server;

import java.util.Deque;
import java.util.Map;
import java.util.Optional;

public class QueryParameterReader
{
	private final Map<String, Deque<String>> parameters;

	public QueryParameterReader(Map<String, Deque<String>> parameters)
	{
		this.parameters = parameters;
	}

	public Optional<String> readString(String name) {
		var deque = parameters.get(name);

		if (deque == null)
			return Optional.empty();

		if (deque.isEmpty())
			return Optional.empty();

		return Optional.of(deque.peekFirst());
	}

	public Optional<Long> readLong(String name) {
		return readString(name)
				.map(Long::parseLong);
	}

	public Optional<Integer> readInt(String name) {
		return readString(name)
				.map(Integer::parseInt);
	}
}
