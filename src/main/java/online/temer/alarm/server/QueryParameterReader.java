package online.temer.alarm.server;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Deque;
import java.util.Map;
import java.util.TimeZone;

public class QueryParameterReader
{
	private final Map<String, Deque<String>> parameters;

	public QueryParameterReader(Map<String, Deque<String>> parameters)
	{
		this.parameters = parameters;
	}

	public String readString(String name)
	{
		var deque = parameters.get(name);

		if (deque == null || deque.isEmpty())
			throw new IncorrectParameter(name);

		String parameter = deque.peekFirst();
		if (parameter == null)
			throw new IncorrectParameter(name);

		return parameter;
	}

	public long readLong(String name)
	{
		return Long.parseLong(readString(name));
	}

	public int readInt(String name)
	{
		return Integer.parseInt(readString(name));
	}

	public ZonedDateTime readTime(String name, TimeZone timeZone)
	{
		return LocalDateTime.parse(readString(name), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
				.atZone(timeZone.toZoneId());
	}
}
