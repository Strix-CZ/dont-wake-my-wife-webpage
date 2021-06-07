package online.temer.alarm.server;

import io.undertow.server.HttpServerExchange;
import online.temer.alarm.dto.DeviceCheckInDto;
import online.temer.alarm.dto.DeviceDto;
import online.temer.alarm.util.Hash;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

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
		Optional<LocalDateTime> time = parameterReader.readTime("time");
		Optional<Integer> battery = parameterReader.readInt("battery");
		Optional<String> hash = parameterReader.readString("hash");

		if (deviceId.isEmpty() || time.isEmpty() || battery.isEmpty() || hash.isEmpty())
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

		long timeOfRequest = time.get().atZone(deviceDto.timeZone.toZoneId()).toEpochSecond();
		long now = ZonedDateTime.now().toEpochSecond();

		if (Math.abs(timeOfRequest - now) > 10)
		{
			exchange.setStatusCode(422);
			exchange.endExchange();
			return;
		}

		String computedHash = calculateHash(deviceId.get(), time.get(), battery.get(), deviceDto.secretKey);

		if (!computedHash.equals(hash.get()))
		{
			exchange.setStatusCode(401);
			exchange.endExchange();
			return;
		}

		var deviceCheckInDto = new DeviceCheckInDto(deviceId.get(), LocalDateTime.now(), battery.get());

		new DeviceCheckInDto.Query(connection)
				.insertUpdate(deviceCheckInDto);
	}

	static String calculateHash(Long deviceId, LocalDateTime time, Integer battery, String secretKey)
	{
		return new Hash()
				.addToMessage(deviceId)
				.addToMessage(" ")
				.addToMessage(time.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
				.addToMessage(" ")
				.addToMessage(battery)
				.calculateHmac(secretKey);
	}
}
