package online.temer.alarm.dto;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class DeviceUpdateDto {
    public final Long id;
    public final long device;
    public final LocalDateTime time;
    public final int battery;

    public DeviceUpdateDto(long device, LocalDateTime time, int battery) {
        this(null, device, time, battery);
    }

    public DeviceUpdateDto(Long id, long device, LocalDateTime time, int battery) {
        this.id = id;
        this.device = device;
        this.time = time;
        this.battery = battery;
    }

    public static class Query {

        private final Connection connection;

        public Query(Connection connection) {
            this.connection = connection;
        }

        public DeviceUpdateDto getLatestUpdate(long device) {
            try {
                return new QueryRunner().query(connection,
                        "SELECT id, kDevice device, time, battery " +
                        "FROM DeviceUpdate " +
                        "WHERE id = (SELECT MAX(id) FROM DeviceUpdate WHERE kDevice = ?)",
                        new Handler(),
                        device);
            }
            catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        public long insertUpdate(DeviceUpdateDto update) {
            try {
                return new QueryRunner().query(connection,
                        "INSERT INTO DeviceUpdate(kDevice, time, battery) " +
                        "VALUES (?, ?, ?) RETURNING id",
                        new ScalarHandler<>(),
                        update.device, update.time, update.battery);
            }
            catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class Handler implements ResultSetHandler<DeviceUpdateDto> {
        @Override
        public DeviceUpdateDto handle(ResultSet rs) throws SQLException {
            if (!rs.next()) {
                return null;
            }

            return new DeviceUpdateDto(
                    rs.getLong("id"),
                    rs.getLong("kDevice"),
                    rs.getTimestamp("time").toLocalDateTime(),
                    rs.getInt("battery")
            );
        }
    }
}
