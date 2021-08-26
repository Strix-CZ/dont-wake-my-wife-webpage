package online.temer.alarm.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Optional;

public class AlarmDto
{
	public final Long device;
	public final boolean isActive;
	public final LocalTime time;
	public final Optional<LocalDate> oneTimeDate;

	public AlarmDto(Long device, boolean isActive, LocalTime time)
	{
		this(device, isActive, time, Optional.empty());
	}

	public AlarmDto(Long device, boolean isActive, LocalTime time, Optional<LocalDate> oneTimeDate)
	{
		this.device = device;
		this.isActive = isActive;
		this.time = time;
		this.oneTimeDate = oneTimeDate;
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
