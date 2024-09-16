package org.example.db;

import com.codahale.metrics.MetricRegistry;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.example.config.ConfigLoader;
import org.example.constants.DatabaseType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLite implements Database {
    private static final Logger logger = LoggerFactory.getLogger(SQLite.class);
    private final String fileName;
    private final MetricRegistry metricRegistry;

    public SQLite(String fileName, MetricRegistry metricRegistry) {
        this.fileName = fileName;
        this.metricRegistry = metricRegistry;
        createNewDatabase();
    }

    @Override
    public DataSource createDataSource(ConfigLoader configLoader) {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.sqlite.JDBC");
        config.setJdbcUrl(configLoader.getDbUrl(DatabaseType.SQLITE));

        config.setMetricRegistry(this.metricRegistry);

        config.setConnectionTimeout(20_000);
        config.setIdleTimeout(300_000);
        config.setMaxLifetime(1_800_000);
        config.setInitializationFailTimeout(0);
        config.setLeakDetectionThreshold(2_000);

        return new HikariDataSource(config);
    }

    private void createNewDatabase() {
        String url = "jdbc:sqlite:" + this.fileName;

        try (Connection conn = DriverManager.getConnection(url);
             Statement statement = conn.createStatement()) {
            if (conn != null) {
                logger.info("A new database has been created or existing one is accessed.");
            }
        } catch (SQLException e) {
            logger.error("Failed to create or access the database", e);
        }
    }
}
