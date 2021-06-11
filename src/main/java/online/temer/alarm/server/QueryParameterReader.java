package online.temer.alarm.server;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Deque;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

public class QueryParameterReader
{
	private final Map<String, Deque<String>> parameters;

	public QueryParameterReader(Map<String, Deque<String>> parameters)
	{
		this.parameters = parameters;
	}

	public boolean hasParameter(String name)
	{
		Optional<String> parameter = readString(name);
		if (parameter.isEmpty())
		{
			return false;
		}

		return !parameter.get().isEmpty();
	}

	public Optional<String> readString(String name)
	{
		var deque = parameters.get(name);

		if (deque == null)
			return Optional.empty();

		if (deque.isEmpty())
			return Optional.empty();

		return Optional.of(deque.peekFirst());
	}

	public Optional<Long> readLong(String name)
	{
		return readString(name)
				.map(Long::parseLong);
	}

	public Optional<Integer> readInt(String name)
	{
		return readString(name)
				.map(Integer::parseInt);
	}

	public Optional<ZonedDateTime> readTime(String name, TimeZone timeZone)
	{
		return readString(name)
				.map(timeString -> LocalDateTime
						.parse(timeString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
						.atZone(timeZone.toZoneId()));
	}
}
