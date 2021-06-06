package online.temer.alarm.dto;

import online.temer.alarm.db.DbTestExtension;
import online.temer.alarm.db.TestConnectionProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDateTime;

@ExtendWith(DbTestExtension.class)
class DeviceDtoTest
{
	private DeviceDto.Query query;

	@BeforeEach
	void setUp()
	{
		query = new DeviceDto.Query(new TestConnectionProvider().get());
	}

	@Test
	public void getNonExistentDevice_returnsNull()
	{
		Assertions.assertNull(query.get(-1000));
	}

	@Test
	void savingAndLoading_returnsSameDevice()
	{
		DeviceDto savedDevice = query.insertDevice();
		DeviceDto loadedDevice = query.get(savedDevice.id);

		Assertions.assertNotNull(loadedDevice, "loadedDevice should not be null");
		Assertions.assertTrue(savedDevice.id > 0, "id was less or equal to zero");
		Assertions.assertEquals(savedDevice.id, loadedDevice.id, "id");
		Assertions.assertEquals(savedDevice.timeCreated, loadedDevice.timeCreated, "timeCreated");
	}

	@Test
	void correctTimeCreatedTest()
	{
		LocalDateTime before = LocalDateTime.now().withNano(0);
		DeviceDto device = query.insertDevice();
		LocalDateTime after = LocalDateTime.now().withNano(0);

		Assertions.assertTrue(
				before.isBefore(device.timeCreated) || before.isEqual(device.timeCreated),
				"timeCreated is too early");

		Assertions.assertTrue(
				after.isAfter(device.timeCreated) || after.isEqual(device.timeCreated),
				"timeCreated is too late");
	}
}
