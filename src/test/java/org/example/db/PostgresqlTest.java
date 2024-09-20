package org.example.db;

import com.codahale.metrics.MetricRegistry;
import com.zaxxer.hikari.HikariDataSource;
import org.example.config.ConfigLoader;
import org.example.constants.DatabaseType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PostgresqlTest {
    private MetricRegistry metricRegistry;
    private ConfigLoader configLoader;
    private Postgresql postgresql;

    @BeforeEach
    void setUp() {
        metricRegistry = mock(MetricRegistry.class);
        configLoader = mock(ConfigLoader.class);
        postgresql = new Postgresql(metricRegistry);
    }

    @Test
    void testCreateDataSource() {
        when(configLoader.getDbUrl(DatabaseType.POSTGRES)).thenReturn("jdbc:postgresql://localhost:5432/test");
        when(configLoader.getDbUser(DatabaseType.POSTGRES)).thenReturn("user");
        when(configLoader.getDbPassword(DatabaseType.POSTGRES)).thenReturn("password");

        DataSource dataSource = postgresql.createDataSource(configLoader);

        assertNotNull(dataSource);
        assertTrue(dataSource instanceof HikariDataSource);

        HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
        assertEquals("jdbc:postgresql://localhost:5432/test", hikariDataSource.getJdbcUrl());
        assertEquals("user", hikariDataSource.getUsername());
        assertEquals("password", hikariDataSource.getPassword());
        assertEquals(metricRegistry, hikariDataSource.getMetricRegistry());
    }

    @Test
    void testCreateDataSourceWithNullUserAndPassword() {
        when(configLoader.getDbUrl(DatabaseType.POSTGRES)).thenReturn("jdbc:postgresql://localhost:5432/test");
        when(configLoader.getDbUser(DatabaseType.POSTGRES)).thenReturn(null);
        when(configLoader.getDbPassword(DatabaseType.POSTGRES)).thenReturn(null);

        DataSource dataSource = postgresql.createDataSource(configLoader);

        assertNotNull(dataSource);
        assertTrue(dataSource instanceof HikariDataSource);

        HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
        assertEquals("jdbc:postgresql://localhost:5432/test", hikariDataSource.getJdbcUrl());
        assertNull(hikariDataSource.getUsername());
        assertNull(hikariDataSource.getPassword());
    }

}