package online.temer.alarm.dto;

import online.temer.alarm.db.DbTestExtension;
import online.temer.alarm.db.TestConnectionProvider;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.TimeZone;

@ExtendWith(DbTestExtension.class)
class DeviceDtoTest
{
	private DeviceQuery deviceQuery;
	private UserQuery userQuery;
	private Connection connection;

	@BeforeEach
	void setUp()
	{
		connection = new TestConnectionProvider().get();
		deviceQuery = new DeviceQuery();
		userQuery = new UserQuery();
	}

	@Test
	public void getNonExistentDevice_returnsNull()
	{
		Assertions.assertThat(deviceQuery.get(connection, -1000))
				.isNull();
	}

	@Test
	void savingAndLoading_returnsSameDevice()
	{
		TimeZone timeZone = TimeZone.getTimeZone(ZoneId.of("America/New_York"));
		LocalDateTime timeCreated = LocalDateTime.now().withNano(0);
		long id = deviceQuery.insertDevice(connection, new DeviceDto(timeCreated, timeZone, "secretKey", null));
		DeviceDto device = deviceQuery.get(connection, id);

		Assertions.assertThat(device)
				.as("loadedDevice should not be null")
				.isNotNull();

		Assertions.assertThat(device.id)
				.as("id")
				.isGreaterThan(0);

		Assertions.assertThat(device.timeCreated)
				.as("timeCreated")
				.isEqualTo(timeCreated);

		Assertions.assertThat(device.timeZone)
				.as("timeZone")
				.isEqualTo(timeZone);

		Assertions.assertThat(device.secretKey)
				.as("secretKey")
				.isEqualTo("secretKey");
	}

	@Test
	void whenThereAreMultipleDevices_getWithoutIdReturnsAnyOfThem()
	{
		deviceQuery.generateSaveAndLoadDevice(connection);
		deviceQuery.generateSaveAndLoadDevice(connection);

		Assertions.assertThat(deviceQuery.get(connection))
				.isNotNull();
	}

	@Test
	void testGettingDeviceByOwner()
	{
		var user = userQuery.createInsertAndLoadUser(connection, "john@example.com", "bar");
		long device1 = deviceQuery.insertDevice(connection, new DeviceDto(LocalDateTime.now(), TimeZone.getDefault(), "secretKey", user.id));
		long device2 = deviceQuery.insertDevice(connection, new DeviceDto(LocalDateTime.now(), TimeZone.getDefault(), "secretKey", user.id));

		List<DeviceDto> devices = deviceQuery.getByOwner(connection, user.id);
		Assertions.assertThat(devices)
				.extracting(device -> device.id)
				.containsExactlyInAnyOrder(device1, device2);
	}
}
