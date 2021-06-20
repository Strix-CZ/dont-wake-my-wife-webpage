package online.temer.alarm.dto;

import java.time.LocalTime;

public class AlarmDto
{
	public final Long device;
	public final LocalTime time;

	public AlarmDto(Long device, LocalTime time)
	{
		this.device = device;
		this.time = time;
	}
}
