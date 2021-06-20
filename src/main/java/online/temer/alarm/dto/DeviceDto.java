package online.temer.alarm.dto;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.TimeZone;
import java.util.stream.Collectors;

public class DeviceDto
{
	public final Long id;
	public final LocalDateTime timeCreated;
	public final TimeZone timeZone;
	public final String secretKey;

	public DeviceDto(LocalDateTime timeCreated, TimeZone timeZone, String secretKey)
	{
		this(null, timeCreated, timeZone, secretKey);
	}

	public DeviceDto(Long id, LocalDateTime timeCreated, TimeZone timeZone, String secretKey)
	{
		this.id = id;
		this.timeCreated = timeCreated;
		this.timeZone = timeZone;
		this.secretKey = secretKey;
	}

	public static DeviceDto generateDevice()
	{
		return generateDevice(TimeZone.getDefault());
	}

	public static DeviceDto generateDevice(TimeZone timeZone)
	{
		String possibleLetters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ01234567890";

		String secretKey = new SecureRandom().ints()
				.limit(40)
				.mapToObj(r -> "" + possibleLetters.charAt(Math.abs(r) % possibleLetters.length()))
				.collect(Collectors.joining());

		return new DeviceDto(
				LocalDateTime.now(),
				timeZone,
				secretKey
		);
	}
}
