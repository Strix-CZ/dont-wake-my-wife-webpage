package online.temer.alarm.dto;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.TimeZone;
import java.util.stream.Collectors;

public class DeviceDto
{
	public final Long id;
	public final LocalDateTime timeCreated;
	public final TimeZone timeZone;
	public final String secretKey;
	public final Long owner;

	public DeviceDto(LocalDateTime timeCreated, TimeZone timeZone, String secretKey, Long owner)
	{
		this(null, timeCreated, timeZone, secretKey, owner);
	}

	public DeviceDto(Long id, LocalDateTime timeCreated, TimeZone timeZone, String secretKey, Long owner)
	{
		this.id = id;
		this.timeCreated = timeCreated;
		this.timeZone = timeZone;
		this.secretKey = secretKey;
		this.owner = owner;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		DeviceDto deviceDto = (DeviceDto) o;
		return Objects.equals(id, deviceDto.id) && timeCreated.equals(deviceDto.timeCreated) && timeZone.equals(deviceDto.timeZone) && secretKey.equals(deviceDto.secretKey) && Objects.equals(owner, deviceDto.owner);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(id, timeCreated, timeZone, secretKey, owner);
	}

	public static DeviceDto generateDevice()
	{
		return generateDevice(TimeZone.getDefault(), null);
	}

	public static DeviceDto generateDevice(TimeZone timeZone, Long owner)
	{
		String possibleLetters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ01234567890";

		String secretKey = new SecureRandom().ints()
				.limit(40)
				.mapToObj(r -> "" + possibleLetters.charAt(Math.abs(r) % possibleLetters.length()))
				.collect(Collectors.joining());

		return new DeviceDto(
				LocalDateTime.now(),
				timeZone,
				secretKey,
				owner);
	}
}
