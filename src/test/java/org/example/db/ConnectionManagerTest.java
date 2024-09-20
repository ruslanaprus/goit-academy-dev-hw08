package org.example.db;

import com.codahale.metrics.MetricRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConnectionManagerTest {

    private Database database;
    private MetricRegistry metricRegistry;
    private ConnectionManager connectionManager;
    private DataSource dataSource;
    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        database = mock(Database.class);
        metricRegistry = mock(MetricRegistry.class);
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        when(database.createDataSource(any())).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);

        connectionManager = ConnectionManager.getInstance(database, metricRegistry);
    }

    @AfterEach
    void tearDown() throws Exception {
        Field instance = ConnectionManager.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);
        instance.setAccessible(false);
    }

    @Test
    void testGetInstanceSingleton() {
        ConnectionManager instance1 = ConnectionManager.getInstance(database, metricRegistry);
        ConnectionManager instance2 = ConnectionManager.getInstance(database, metricRegistry);
        assertSame(instance1, instance2);
    }

    @Test
    void testGetConnection() throws SQLException {
        Connection conn = connectionManager.getConnection();
        assertNotNull(conn);
        verify(dataSource, times(1)).getConnection();
    }

    @Test
    void testGetConnectionFailure() throws SQLException {
        when(dataSource.getConnection()).thenThrow(new SQLException("Database connection failed"));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            connectionManager.getConnection();
        });
        assertEquals("Database connection is unavailable. Please contact support.", exception.getMessage());
    }

}