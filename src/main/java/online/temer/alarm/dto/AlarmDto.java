package online.temer.alarm.dto;

import java.time.LocalTime;
import java.util.Objects;

public class AlarmDto
{
	public final Long device;
	public final LocalTime time;

	public AlarmDto(Long device, LocalTime time)
	{
		this.device = device;
		this.time = time;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		AlarmDto alarmDto = (AlarmDto) o;
		return Objects.equals(device, alarmDto.device) && Objects.equals(time, alarmDto.time);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(device, time);
	}
}
