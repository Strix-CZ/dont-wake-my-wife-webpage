package online.temer.alarm.dto;

import online.temer.alarm.util.DbTestExtension;
import online.temer.alarm.util.TestConnectionProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.sql.Connection;
import java.sql.SQLException;

@ExtendWith(DbTestExtension.class)
public class DeviceUpdateDtoTest {

    private Connection connection;

    @BeforeEach
    public void setUp() {
        connection = TestConnectionProvider.getConnection();
    }

    @Test
    public void testConnection() throws SQLException {
        Assertions.assertNotNull(connection);
        Assertions.assertTrue(connection.isValid(1));
    }
}
