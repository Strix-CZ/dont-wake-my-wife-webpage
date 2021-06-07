package online.temer.alarm.server;

import io.undertow.server.HttpServerExchange;
import online.temer.alarm.dto.DeviceCheckInDto;
import online.temer.alarm.dto.DeviceDto;

import java.sql.Connection;
import java.time.LocalDateTime;
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
		Optional<Integer> battery = parameterReader.readInt("battery");

		if (deviceId.isEmpty() || battery.isEmpty())
		{
			exchange.setStatusCode(400);
			return;
		}

		DeviceDto deviceDto = new DeviceDto.Query(connection)
				.get(deviceId.get());

		if (deviceDto == null)
		{
			exchange.setStatusCode(400);
			return;
		}

		var deviceCheckInDto = new DeviceCheckInDto(deviceId.get(), LocalDateTime.now(), battery.get());

		new DeviceCheckInDto.Query(connection)
				.insertUpdate(deviceCheckInDto);
	}
}
