package online.temer.alarm.util;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

public class DateTimeUtil
{
	public static String formatCurrentTime(TimeZone deviceTimeZone)
	{
		return ZonedDateTime.now(deviceTimeZone.toZoneId())
				.withNano(0)
				.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
	}
}
