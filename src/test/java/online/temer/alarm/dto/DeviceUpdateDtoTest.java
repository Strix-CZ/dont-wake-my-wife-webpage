package online.temer.alarm.dto;

import online.temer.alarm.db.DbTestExtension;
import online.temer.alarm.db.TestConnectionProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.sql.Connection;
import java.time.LocalDateTime;

@ExtendWith(DbTestExtension.class)
class DeviceUpdateDtoTest
{
	private DeviceUpdateDto.Query query;
	private DeviceDto.Query deviceQuery;
	private long deviceId;

	@BeforeEach
	void setUp()
	{
		Connection connection = new TestConnectionProvider().get();
		query = new DeviceUpdateDto.Query(connection);
		deviceQuery = new DeviceDto.Query(connection);

		deviceId = deviceQuery.insertDevice().id;
	}

	@Test
	public void whenNoUpdates_getLatestReturnsNull()
	{
		Assertions.assertNull(query.getLatestUpdate(1));
	}

	@Test
	void whenSingleUpdate_returnsIt()
	{
		LocalDateTime time = LocalDateTime.now().withNano(0);
		query.insertUpdate(new DeviceUpdateDto(deviceId, time, 100));

		var deviceUpdate = query.getLatestUpdate(deviceId);

		Assertions.assertNotNull(deviceUpdate, "deviceUpdate should not be null");
		Assertions.assertTrue(deviceUpdate.id > 0, "id");
		Assertions.assertEquals(deviceId, deviceUpdate.device, "deviceId");
		Assertions.assertEquals(time, deviceUpdate.time, "time");
		Assertions.assertEquals(100, deviceUpdate.battery, "battery");
	}

	@Test
	void whenMultipleUpdates_latestIsReturned()
	{
		query.insertUpdate(new DeviceUpdateDto(deviceId, LocalDateTime.now(), 100));
		query.insertUpdate(new DeviceUpdateDto(deviceId, LocalDateTime.now(), 90));

		var deviceUpdate = query.getLatestUpdate(deviceId);
		Assertions.assertEquals(90, deviceUpdate.battery, "battery");
	}

	@Test
	void whenMultipleDevicesUpdates_latestOfTheDeviceIsReturned()
	{
		query.insertUpdate(new DeviceUpdateDto(deviceId, LocalDateTime.now(), 100));

		long deviceId2 = deviceQuery.insertDevice().id;
		query.insertUpdate(new DeviceUpdateDto(deviceId2, LocalDateTime.now(), 50));

		Assertions.assertEquals(100, query.getLatestUpdate(deviceId).battery, "battery of first device is 100");
		Assertions.assertEquals(50, query.getLatestUpdate(deviceId2).battery, "battery of second device is 50");
	}
}
