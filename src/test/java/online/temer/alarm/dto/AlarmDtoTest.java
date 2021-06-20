package online.temer.alarm.dto;

import online.temer.alarm.db.DbTestExtension;
import online.temer.alarm.db.TestConnectionProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.sql.Connection;
import java.time.LocalTime;

@ExtendWith(DbTestExtension.class)
class AlarmDtoTest
{
	private AlarmQuery query;
	private DeviceDto device;
	private Connection connection;

	@BeforeEach
	void setUp()
	{
		connection = new TestConnectionProvider().get();
		device = new DeviceQuery().generateSaveAndLoadDevice(connection);
		query = new AlarmQuery(connection);
	}

	@Test
	public void getAlarmOfNonExistentDevice_returnsNull()
	{
		Assertions.assertNull(query.get(connection, -1000));
	}

	@Test
	public void getNonExistentAlarm_returnsNull()
	{
		Assertions.assertNull(query.get(connection, device.id));
	}

	@Test
	void savingAndLoading_returnsSameAlarm()
	{
		query.insertOrUpdateAlarm(connection, new AlarmDto(device.id, LocalTime.of(23, 50, 10)));
		AlarmDto alarmDto = query.get(connection, device.id);

		Assertions.assertEquals(device.id, alarmDto.device, "device");
		Assertions.assertEquals(LocalTime.of(23, 50, 10), alarmDto.time, "time");
	}

	@Test
	void updatingAlarm_overridesExistingAlarm()
	{
		query.insertOrUpdateAlarm(connection, new AlarmDto(device.id, LocalTime.of(23, 0)));
		query.insertOrUpdateAlarm(connection, new AlarmDto(device.id, LocalTime.of(5, 30)));

		AlarmDto alarmDto = query.get(connection, device.id);
		Assertions.assertEquals(LocalTime.of(5, 30), alarmDto.time, "Alarm was not updated");
	}
}
