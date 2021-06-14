package online.temer.alarm.server;

import online.temer.alarm.dto.AlarmDto;
import online.temer.alarm.dto.DeviceCheckInDto;
import online.temer.alarm.dto.DeviceDto;
import online.temer.alarm.util.Hash;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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
		long deviceId = parameterReader.readLong("device");
		int battery = parameterReader.readInt("battery");
		String hash = parameterReader.readString("hash");

		DeviceDto deviceDto = findDevice(deviceId);

		ZonedDateTime time = parameterReader.readTime("time", deviceDto.timeZone);

		validateTimeOfRequest(deviceDto, time);
		validateHash(deviceId, battery, hash, deviceDto, time);

		logCheckIn(deviceId, battery);

		AlarmDto alarm = new AlarmDto.Query(connection).get(deviceDto.id);

		return new Response(formatCurrentTime(deviceDto.timeZone) + "\n"
				+ formatAlarm(alarm) + "\n");
	}

	private DeviceDto findDevice(long deviceId) {
		DeviceDto deviceDto = new DeviceDto.Query(connection).get(deviceId);

		if (deviceDto == null)
		{
			throw new IncorrectRequest(400, "unknown device");
		}

		return deviceDto;
	}

	private void validateHash(long deviceId, int battery, String hash, DeviceDto deviceDto, ZonedDateTime time) {
		String computedHash = calculateHash(deviceId, time.toLocalDateTime(), battery, deviceDto.secretKey);
		if (!computedHash.equals(hash))
		{
			throw new IncorrectRequest(401, formatCurrentTime(deviceDto.timeZone));
		}
	}

	private void validateTimeOfRequest(DeviceDto deviceDto, ZonedDateTime time) {
		long nowInTimeZoneOfDevice = ZonedDateTime.now(deviceDto.timeZone.toZoneId()).toEpochSecond();
		if (Math.abs(time.toEpochSecond() - nowInTimeZoneOfDevice) > 10)
		{
			throw new IncorrectRequest(422, formatCurrentTime(deviceDto.timeZone));
		}
	}

	private void logCheckIn(long deviceId, int battery)
	{
		var deviceCheckInDto = new DeviceCheckInDto(deviceId, LocalDateTime.now(), battery);
		new DeviceCheckInDto.Query(connection)
				.insertUpdate(deviceCheckInDto);
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
