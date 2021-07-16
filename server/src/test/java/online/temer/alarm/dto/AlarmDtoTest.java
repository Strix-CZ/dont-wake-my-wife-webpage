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
	private DeviceQuery deviceQuery;

	@BeforeEach
	void setUp()
	{
		connection = new TestConnectionProvider().get();
		deviceQuery = new DeviceQuery();
		device = deviceQuery.generateSaveAndLoadDevice(connection);
		query = new AlarmQuery();
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
		query.insertOrUpdate(connection, new AlarmDto(device.id, true, LocalTime.of(23, 50, 10)));
		AlarmDto alarmDto = query.get(connection, device.id);

		Assertions.assertEquals(device.id, alarmDto.device, "device");
		Assertions.assertTrue(alarmDto.isActive, "isActive");
		Assertions.assertEquals(LocalTime.of(23, 50, 10), alarmDto.time, "time");
	}

	@Test
	void updatingAlarm_overridesExistingAlarm()
	{
		query.insertOrUpdate(connection, new AlarmDto(device.id, true, LocalTime.of(23, 0)));
		query.insertOrUpdate(connection, new AlarmDto(device.id, false, LocalTime.of(5, 30)));

		AlarmDto alarmDto = query.get(connection, device.id);
		Assertions.assertEquals(LocalTime.of(5, 30), alarmDto.time, "Time of alarm was not updated");
		Assertions.assertFalse(alarmDto.isActive, "Is active was not updated");
	}

	@Test
	void noAlarm_deleteDoesNothing()
	{
		query.delete(connection, device.id);
	}

	@Test
	void deleteAlarm_deletes()
	{
		query.insertOrUpdate(connection, new AlarmDto(device.id, true, LocalTime.of(23, 0)));
		query.delete(connection, device.id);

		Assertions.assertNull(query.get(connection, device.id));
	}

	@Test
	void deleteAlarm_doesNotDeleteAlarmOfOtherDevice()
	{
		var otherDevice = deviceQuery.generateSaveAndLoadDevice(connection);
		query.insertOrUpdate(connection, new AlarmDto(otherDevice.id, true, LocalTime.of(20, 0)));

		query.insertOrUpdate(connection, new AlarmDto(device.id, false, LocalTime.of(10, 0)));
		query.delete(connection, device.id);

		AlarmDto alarmDto = query.get(connection, otherDevice.id);
		Assertions.assertNotNull(alarmDto, "The alarm was deleted");
	}
}
