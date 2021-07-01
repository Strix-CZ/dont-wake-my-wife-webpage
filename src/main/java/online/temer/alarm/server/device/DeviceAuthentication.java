package online.temer.alarm.server.device;

import online.temer.alarm.dto.DeviceDto;
import online.temer.alarm.dto.DeviceQuery;
import online.temer.alarm.server.Handler.Response;
import online.temer.alarm.server.QueryParameterReader;
import online.temer.alarm.server.authentication.Authentication;
import online.temer.alarm.util.DateTimeUtil;
import online.temer.alarm.util.Hash;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DeviceAuthentication implements Authentication<DeviceDto>
{
	private final DeviceQuery deviceQuery;

	public DeviceAuthentication(DeviceQuery deviceQuery)
	{
		this.deviceQuery = deviceQuery;
	}

	public Result<DeviceDto> authenticate(Connection connection, QueryParameterReader parameterReader)
	{
		long deviceId = parameterReader.readLong("device");

		DeviceDto deviceDto = deviceQuery.get(connection, deviceId);
		if (deviceDto == null)
		{
			return new Result<>(new Response(400, "unknown device"));
		}

		ZonedDateTime time = parameterReader.readTime("time", deviceDto.timeZone);
		String hash = parameterReader.readString("hash");

		if (!isTimeOfRequestInTolerance(deviceDto, time))
		{
			return new Result<>(new Response(422, DateTimeUtil.formatCurrentTime(deviceDto.timeZone)));
		}

		String computedHash = calculateHash(deviceId, time.toLocalDateTime(), deviceDto.secretKey);
		if (!computedHash.equals(hash))
		{
			return new Result<>(new Response(401, DateTimeUtil.formatCurrentTime(deviceDto.timeZone)));
		}

		return new Result<>(deviceDto);
	}

	private boolean isTimeOfRequestInTolerance(DeviceDto deviceDto, ZonedDateTime time)
	{
		long nowInTimeZoneOfDevice = ZonedDateTime.now(deviceDto.timeZone.toZoneId()).toEpochSecond();
		return Math.abs(time.toEpochSecond() - nowInTimeZoneOfDevice) <= 10;
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
