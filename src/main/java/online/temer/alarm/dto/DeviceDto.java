package online.temer.alarm.dto;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class DeviceDto {
    public final long id;
    public final LocalDateTime timeCreated;

    public DeviceDto(long id, LocalDateTime timeCreated) {
        this.id = id;
        this.timeCreated = timeCreated;
    }

    public static class Query {

        private final Connection connection;

        public Query(Connection connection) {
            this.connection = connection;
        }

        public DeviceDto insertDevice() {
            try {
                Long id = new QueryRunner().query(connection, "INSERT INTO Device() VALUES () RETURNING id", new ScalarHandler<>());
                return get(id);
            }
            catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        public DeviceDto get(long id) {
            try {
                return new QueryRunner().query(
                        connection,
                        "SELECT id, timeCreated FROM Device WHERE id = ?",
                        new Handler(),
                        id);
            }
            catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class Handler implements ResultSetHandler<DeviceDto> {
        @Override
        public DeviceDto handle(ResultSet rs) throws SQLException {
            if (!rs.next()) {
                return null;
            }

            return new DeviceDto(
                    rs.getLong("id"),
                    rs.getTimestamp("timeCreated").toLocalDateTime()
            );
        }
    }
}
