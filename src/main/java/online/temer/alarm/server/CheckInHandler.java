package online.temer.alarm.server;

import online.temer.alarm.dto.AlarmDto;
import online.temer.alarm.dto.DeviceCheckInDto;
import online.temer.alarm.dto.DeviceDto;
import online.temer.alarm.util.Hash;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.TimeZone;

public class CheckInHandler extends Handler
{
	private final Connection connection;

	public CheckInHandler(Connection connection)
	{
		this.connection = connection;
	}

	public Response handle(QueryParameterReader parameterReader)
	{
		Optional<Long> deviceId = parameterReader.readLong("device");
		Optional<Integer> battery = parameterReader.readInt("battery");
		Optional<String> hash = parameterReader.readString("hash");

		if (deviceId.isEmpty() || !parameterReader.hasParameter("time") || battery.isEmpty() || hash.isEmpty())
		{
			return new Response(400);
		}

		DeviceDto deviceDto = new DeviceDto.Query(connection)
				.get(deviceId.get());

		if (deviceDto == null)
		{
			return new Response(400);
		}

		Optional<ZonedDateTime> time = parameterReader.readTime("time", deviceDto.timeZone);
		if (time.isEmpty())
		{
			return new Response(400);
		}

		if (!isCorrectTimeOfRequest(deviceDto.timeZone, time.get()))
		{
			return new Response(422, formatCurrentTime(deviceDto.timeZone));
		}

		String computedHash = calculateHash(deviceId.get(), time.get().toLocalDateTime(), battery.get(), deviceDto.secretKey);
		if (!computedHash.equals(hash.get()))
		{
			return new Response(401, formatCurrentTime(deviceDto.timeZone));
		}

		var deviceCheckInDto = new DeviceCheckInDto(deviceId.get(), LocalDateTime.now(), battery.get());
		new DeviceCheckInDto.Query(connection)
				.insertUpdate(deviceCheckInDto);

		AlarmDto alarm = new AlarmDto.Query(connection).get(deviceDto.id);

		return new Response(formatCurrentTime(deviceDto.timeZone) + "\n"
				+ formatAlarm(alarm) + "\n");
	}

	private boolean isCorrectTimeOfRequest(TimeZone timeZoneOfDevice, ZonedDateTime timeOfRequest) {
		long nowInTimeZoneOfDevice = ZonedDateTime.now(timeZoneOfDevice.toZoneId()).toEpochSecond();
		return Math.abs(timeOfRequest.toEpochSecond() - nowInTimeZoneOfDevice) <= 10;
	}

	private String formatAlarm(AlarmDto alarm) {
		if (alarm == null)
		{
			return "none";
		}
		else
		{
			return alarm.time.format(DateTimeFormatter.ISO_LOCAL_TIME);
		}
	}

	private String formatCurrentTime(TimeZone deviceTimeZone)
	{
		return ZonedDateTime.now(deviceTimeZone.toZoneId())
						.withNano(0)
						.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
	}

	static String calculateHash(Long deviceId, LocalDateTime time, Integer battery, String secretKey)
	{
		return new Hash()
				.addToMessage(deviceId)
				.addToMessage(" ")
				.addToMessage(time.withNano(0).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
				.addToMessage(" ")
				.addToMessage(battery)
				.calculateHmac(secretKey);
	}
}
