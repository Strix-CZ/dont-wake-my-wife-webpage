package online.temer.alarm.dto;

import online.temer.alarm.db.DbTestExtension;
import online.temer.alarm.db.TestConnectionProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.TimeZone;

@ExtendWith(DbTestExtension.class)
class DeviceDtoTest
{
	private DeviceQuery query;
	private Connection connection;

	@BeforeEach
	void setUp()
	{
		connection = new TestConnectionProvider().get();
		query = new DeviceQuery();
	}

	@Test
	public void getNonExistentDevice_returnsNull()
	{
		Assertions.assertNull(query.get(connection, -1000));
	}

	@Test
	void savingAndLoading_returnsSameDevice()
	{
		TimeZone timeZone = TimeZone.getTimeZone(ZoneId.of("America/New_York"));
		LocalDateTime timeCreated = LocalDateTime.now().withNano(0);
		long id = query.insertDevice(connection, new DeviceDto(timeCreated, timeZone, "secretKey"));
		DeviceDto device = query.get(connection, id);

		Assertions.assertNotNull(device, "loadedDevice should not be null");
		Assertions.assertTrue(device.id > 0, "id was less or equal to zero");
		Assertions.assertEquals(timeCreated, device.timeCreated, "timeCreated");
		Assertions.assertEquals(timeZone, device.timeZone, "timeZone");
		Assertions.assertEquals("secretKey", device.secretKey, "secretKey");
	}

	@Test
	void whenThereAreMultipleDevices_getWithoutIdReturnsAnyOfThem()
	{
		query.generateSaveAndLoadDevice(connection);
		query.generateSaveAndLoadDevice(connection);

		Assertions.assertNotNull(query.get(connection));
	}
}
