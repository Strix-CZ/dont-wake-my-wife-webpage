package online.temer.alarm.dto;

import online.temer.alarm.db.DbTestExtension;
import online.temer.alarm.db.TestConnectionProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.TimeZone;

@ExtendWith(DbTestExtension.class)
class AlarmDtoTest
{
	private AlarmDto.Query query;
	private DeviceDto device;

	@BeforeEach
	void setUp()
	{
		device = new DeviceDto.Query(new TestConnectionProvider().get()).generateSaveAndLoadDevice();
		query = new AlarmDto.Query(new TestConnectionProvider().get());
	}

	@Test
	public void getAlarmOfNonExistentDevice_returnsNull()
	{
		Assertions.assertNull(query.get(-1000));
	}

	@Test
	public void getNonExistentAlarm_returnsNull()
	{
		Assertions.assertNull(query.get(device.id));
	}

	@Test
	void savingAndLoading_returnsSameAlarm()
	{
		query.insertOrUpdateAlarm(new AlarmDto(device.id, LocalTime.of(23, 50, 10)));
		AlarmDto alarmDto = query.get(device.id);

		Assertions.assertEquals(device.id, alarmDto.device, "device");
		Assertions.assertEquals(LocalTime.of(23, 50, 10), alarmDto.time, "time");
	}

	@Test
	void updatingAlarm_overridesExistingAlarm()
	{
		query.insertOrUpdateAlarm(new AlarmDto(device.id, LocalTime.of(23, 0)));
		query.insertOrUpdateAlarm(new AlarmDto(device.id, LocalTime.of(5, 30)));

		AlarmDto alarmDto = query.get(device.id);
		Assertions.assertEquals(LocalTime.of(5, 30), alarmDto.time, "Alarm was not updated");
	}
}