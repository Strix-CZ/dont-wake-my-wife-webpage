package online.temer.alarm.dto;

import java.time.LocalDateTime;
import java.util.Objects;

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

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		DeviceCheckInDto that = (DeviceCheckInDto) o;
		return device == that.device && battery == that.battery && Objects.equals(id, that.id) && Objects.equals(time, that.time);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(id, device, time, battery);
	}
}
