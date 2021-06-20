package online.temer.alarm.dto;

import java.time.LocalDateTime;

public class DeviceCheckInDto
{
	public final Long id;
	public final long device;
	public final LocalDateTime time;
	public final int battery;

	public DeviceCheckInDto(long device, LocalDateTime time, int battery)
	{
		this(null, device, time, battery);
	}

	public DeviceCheckInDto(Long id, long device, LocalDateTime time, int battery)
	{
		this.id = id;
		this.device = device;
		this.time = time;
		this.battery = battery;
	}
}
