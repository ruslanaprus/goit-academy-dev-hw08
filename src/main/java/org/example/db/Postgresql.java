package org.example.db;

import com.codahale.metrics.MetricRegistry;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.example.config.ConfigLoader;
import org.example.constants.DatabaseType;

import javax.sql.DataSource;

public class Postgresql implements Database {
    private final MetricRegistry metricRegistry;

    public Postgresql(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    @Override
    public DataSource createDataSource(ConfigLoader configLoader) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(configLoader.getDbUrl(DatabaseType.POSTGRES));
        if (configLoader.getDbUser(DatabaseType.POSTGRES) != null) {
            config.setUsername(configLoader.getDbUser(DatabaseType.POSTGRES));
        }
        if (configLoader.getDbPassword(DatabaseType.POSTGRES) != null) {
            config.setPassword(configLoader.getDbPassword(DatabaseType.POSTGRES));
        }

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        config.setMetricRegistry(this.metricRegistry);

        config.setConnectionTimeout(20_000);
        config.setIdleTimeout(300_000);
        config.setMaxLifetime(1_800_000);
        config.setMinimumIdle(1);
        config.setMaximumPoolSize(15);
        config.setInitializationFailTimeout(0);
        config.setLeakDetectionThreshold(2_000);

        return new HikariDataSource(config);
    }
}