package online.temer.alarm.server;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpServerExchange;
import online.temer.alarm.db.ConnectionProvider;
import online.temer.alarm.dto.DeviceDto;
import online.temer.alarm.dto.DeviceUpdateDto;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.Optional;

public class Server
{
	private final Undertow server;
	private final ConnectionProvider connectionProvider;

	public Server(int port, String host, ConnectionProvider connectionProvider)
	{
		this.connectionProvider = connectionProvider;
		server = createServer(port, host);
	}

	public void start()
	{
		server.start();
	}

	public void stop()
	{
		server.stop();
	}

	private Undertow createServer(int port, String host)
	{
		return Undertow.builder()
				.addHttpListener(port, host)
				.setHandler(Handlers.path().addExactPath("/checkin", this::handleCheckIn))
				.build();
	}

	private void handleCheckIn(HttpServerExchange exchange)
	{
		var parameterReader = new QueryParameterReader(exchange.getQueryParameters());

		Optional<Long> deviceId = parameterReader.readLong("device");
		Optional<Integer> battery = parameterReader.readInt("battery");

		if (deviceId.isEmpty() || battery.isEmpty())
		{
			exchange.setStatusCode(400);
			return;
		}

		Connection connection = connectionProvider.get();
		DeviceDto deviceDto = new DeviceDto.Query(connection)
				.get(deviceId.get());

		if (deviceDto == null)
		{
			exchange.setStatusCode(400);
			return;
		}

		var deviceUpdateDto = new DeviceUpdateDto(deviceId.get(), LocalDateTime.now(), battery.get());

		new DeviceUpdateDto.Query(connection)
				.insertUpdate(deviceUpdateDto);
	}
}
