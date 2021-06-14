package online.temer.alarm.server;

import io.undertow.server.HttpServerExchange;
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

public class CheckInHandler
{
	private final Connection connection;

	public CheckInHandler(Connection connection)
	{
		this.connection = connection;
	}

	public void handle(HttpServerExchange exchange)
	{
		var parameterReader = new QueryParameterReader(exchange.getQueryParameters());

		Optional<Long> deviceId = parameterReader.readLong("device");
		Optional<Integer> battery = parameterReader.readInt("battery");
		Optional<String> hash = parameterReader.readString("hash");

		if (deviceId.isEmpty() || !parameterReader.hasParameter("time") || battery.isEmpty() || hash.isEmpty())
		{
			exchange.setStatusCode(400);
			exchange.endExchange();
			return;
		}

		DeviceDto deviceDto = new DeviceDto.Query(connection)
				.get(deviceId.get());

		if (deviceDto == null)
		{
			exchange.setStatusCode(400);
			exchange.endExchange();
			return;
		}

		Optional<ZonedDateTime> time = parameterReader.readTime("time", deviceDto.timeZone);
		if (time.isEmpty())
		{
			exchange.setStatusCode(400);
			exchange.endExchange();
			return;
		}

		long timeOfRequest = time.get().toEpochSecond();
		long now = ZonedDateTime.now(deviceDto.timeZone.toZoneId()).toEpochSecond();

		if (Math.abs(timeOfRequest - now) > 10)
		{
			exchange.setStatusCode(422);
			exchange.getResponseSender().send(formatCurrentTime(deviceDto.timeZone));
			return;
		}

		String computedHash = calculateHash(deviceId.get(), time.get().toLocalDateTime(), battery.get(), deviceDto.secretKey);

		if (!computedHash.equals(hash.get()))
		{
			exchange.setStatusCode(401);
			exchange.getResponseSender().send(formatCurrentTime(deviceDto.timeZone));
			return;
		}

		var deviceCheckInDto = new DeviceCheckInDto(deviceId.get(), LocalDateTime.now(), battery.get());
		new DeviceCheckInDto.Query(connection)
				.insertUpdate(deviceCheckInDto);

		AlarmDto alarm = new AlarmDto.Query(connection).get(deviceDto.id);

		exchange.getResponseSender().send(
				formatCurrentTime(deviceDto.timeZone) + "\n"
				+ formatAlarm(alarm) + "\n");
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
