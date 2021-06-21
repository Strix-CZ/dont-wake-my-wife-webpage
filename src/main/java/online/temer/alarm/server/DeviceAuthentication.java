package online.temer.alarm.server;

import online.temer.alarm.dto.DeviceDto;
import online.temer.alarm.dto.DeviceQuery;
import online.temer.alarm.util.DateTimeUtil;
import online.temer.alarm.util.Hash;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class DeviceAuthentication
{
	private final DeviceQuery deviceQuery;

	public DeviceAuthentication(DeviceQuery deviceQuery)
	{
		this.deviceQuery = deviceQuery;
	}

	public Result authenticate(Connection connection, QueryParameterReader parameterReader)
	{
		long deviceId = parameterReader.readLong("device");

		DeviceDto deviceDto = deviceQuery.get(connection, deviceId);
		if (deviceDto == null)
		{
			return new Result(new Handler.Response(400, "unknown device"));
		}

		ZonedDateTime time = parameterReader.readTime("time", deviceDto.timeZone);
		String hash = parameterReader.readString("hash");

		if (!isTimeOfRequestInTolerance(deviceDto, time))
		{
			return new Result(new Handler.Response(422, DateTimeUtil.formatCurrentTime(deviceDto.timeZone)));
		}

		String computedHash = calculateHash(deviceId, time.toLocalDateTime(), deviceDto.secretKey);
		if (!computedHash.equals(hash))
		{
			return new Result(new Handler.Response(401, DateTimeUtil.formatCurrentTime(deviceDto.timeZone)));
		}

		return new Result(deviceDto);
	}

	private boolean isTimeOfRequestInTolerance(DeviceDto deviceDto, ZonedDateTime time)
	{
		long nowInTimeZoneOfDevice = ZonedDateTime.now(deviceDto.timeZone.toZoneId()).toEpochSecond();
		return Math.abs(time.toEpochSecond() - nowInTimeZoneOfDevice) <= 10;
	}

	public static class Result
	{
		public final Optional<DeviceDto> device;
		public final Handler.Response errorResponse;

		public Result(DeviceDto device)
		{
			this.device = Optional.of(device);
			this.errorResponse = null;
		}

		public Result(Handler.Response errorResponse)
		{
			this.device = Optional.empty();
			this.errorResponse = errorResponse;
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
