package online.temer.alarm.test.util;

import org.junit.jupiter.api.Assertions;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

public class TimeAssertion
{
	private final LocalDateTime before = LocalDateTime.now();
	private LocalDateTime after;

	public void untilNow()
	{
		after = LocalDateTime.now();
	}

	public void assertCurrentTimeIgnoringNanos(LocalDateTime actual)
	{
		LocalDateTime beforeWithoutNanos = before.withNano(0);
		LocalDateTime afterWithoutNanos = after.withNano(0);

		assertTimes(actual, beforeWithoutNanos, afterWithoutNanos, "");
	}

	public void assertCurrentTimeIgnoringNanos(LocalDateTime actual, TimeZone timeZone)
	{
		LocalDateTime beforeWithoutNanos = before
				.withNano(0)
				.atZone(ZoneId.systemDefault())
				.withZoneSameInstant(timeZone.toZoneId())
				.toLocalDateTime();

		LocalDateTime afterWithoutNanos = after
				.withNano(0)
				.atZone(ZoneId.systemDefault())
				.withZoneSameInstant(timeZone.toZoneId())
				.toLocalDateTime();

		assertTimes(actual, beforeWithoutNanos, afterWithoutNanos, "in time zone " + timeZone.getID());
	}

	private void assertTimes(LocalDateTime actual, LocalDateTime before, LocalDateTime after, String extraMessage)
	{
		if (before.isAfter(actual))
		{
			Assertions.fail("Expecting time to be at or after "
					+ extraMessage
					+ " "
					+ before.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
					+ "but was "
					+ actual.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
		}

		if (after.isBefore(actual))
		{
			Assertions.fail("Expecting time to be at or before "
					+ extraMessage
					+ " "
					+ after.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
					+ "but was "
					+ actual.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
		}
	}
}
