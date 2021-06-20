package online.temer.alarm.server;

import online.temer.alarm.dto.DeviceDto;
import online.temer.alarm.dto.DeviceQuery;
import online.temer.alarm.util.DateTimeUtil;
import online.temer.alarm.util.Hash;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DeviceAuthentication
{
	public DeviceDto authenticate(Connection connection, QueryParameterReader parameterReader)
	{
		long deviceId = parameterReader.readLong("device");

		DeviceDto deviceDto = findDevice(deviceId, connection);

		ZonedDateTime time = parameterReader.readTime("time", deviceDto.timeZone);
		String hash = parameterReader.readString("hash");

		validateTimeOfRequest(deviceDto, time);
		validateHash(deviceId, hash, deviceDto, time);

		return deviceDto;
	}

	private DeviceDto findDevice(long deviceId, Connection connection) {
		DeviceDto deviceDto = new DeviceQuery(connection).get(connection, deviceId);

		if (deviceDto == null)
		{
			throw new IncorrectRequest(400, "unknown device");
		}

		return deviceDto;
	}

	private void validateHash(long deviceId, String hash, DeviceDto deviceDto, ZonedDateTime time)
	{
		String computedHash = calculateHash(deviceId, time.toLocalDateTime(), deviceDto.secretKey);
		if (!computedHash.equals(hash))
		{
			throw new IncorrectRequest(401, DateTimeUtil.formatCurrentTime(deviceDto.timeZone));
		}
	}

	private void validateTimeOfRequest(DeviceDto deviceDto, ZonedDateTime time)
	{
		long nowInTimeZoneOfDevice = ZonedDateTime.now(deviceDto.timeZone.toZoneId()).toEpochSecond();
		if (Math.abs(time.toEpochSecond() - nowInTimeZoneOfDevice) > 10)
		{
			throw new IncorrectRequest(422, DateTimeUtil.formatCurrentTime(deviceDto.timeZone));
		}
	}

	static String calculateHash(Long deviceId, LocalDateTime time, String secretKey)
	{
		return new Hash()
				.addToMessage(deviceId)
				.addToMessage(" ")
				.addToMessage(time.withNano(0).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
				.calculateHmac(secretKey);
	}
}
