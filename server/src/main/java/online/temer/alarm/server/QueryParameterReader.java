package online.temer.alarm.server;

import spark.Request;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

public class QueryParameterReader
{
	private final Map<String, List<String>> parameters;

	public QueryParameterReader(Request request)
	{
		parameters = request.queryParams().stream()
				.collect(Collectors.toMap(
						k -> k,
						k -> Arrays.asList(request.queryParamsValues(k))));
	}

	public QueryParameterReader(String... keysAndValues)
	{
		if (keysAndValues.length % 2 == 1)
		{
			throw new IllegalArgumentException("Parameters must be alternating keys and values, but received odd number of parameters.");
		}

		parameters = new HashMap<>(keysAndValues.length / 2);
		for (int i = 0; i < keysAndValues.length; i += 2)
		{
			String key = keysAndValues[i];
			String value = keysAndValues[i + 1];

			parameters.computeIfAbsent(key, k -> new LinkedList<>())
					.add(value);
		}
	}

	public String readString(String name)
	{
		var deque = parameters.get(name);

		if (deque == null || deque.isEmpty())
			throw new IncorrectParameter(name);

		String parameter = deque.get(0);
		if (parameter == null)
			throw new IncorrectParameter(name);

		return parameter;
	}

	public long readLong(String name)
	{
		try
		{
			return Long.parseLong(readString(name));
		}
		catch (NumberFormatException e)
		{
			throw new IncorrectParameter(name);
		}
	}

	public int readInt(String name)
	{
		try
		{
			return Integer.parseInt(readString(name));
		}
		catch (NumberFormatException e)
		{
			throw new IncorrectParameter(name);
		}
	}

	public ZonedDateTime readTime(String name, TimeZone timeZone)
	{
		try
		{
			return LocalDateTime.parse(readString(name), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
					.atZone(timeZone.toZoneId());
		}
		catch (DateTimeParseException e) {
			throw new IncorrectParameter(name);
		}
	}
}
