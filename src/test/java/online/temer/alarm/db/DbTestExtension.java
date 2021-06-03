package online.temer.alarm.db;

import org.junit.jupiter.api.extension.*;

import java.sql.Connection;
import java.sql.SQLException;

public class DbTestExtension implements
        BeforeAllCallback,
        AfterAllCallback,
        AfterEachCallback {

    private Connection connection;

    @Override
    public void beforeAll(ExtensionContext context) throws SQLException {
        connection = TestConnectionProvider.getConnection();
        connection.setAutoCommit(false);
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) {
        TestConnectionProvider.close();
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) throws SQLException {
        connection.rollback();
    }
}
