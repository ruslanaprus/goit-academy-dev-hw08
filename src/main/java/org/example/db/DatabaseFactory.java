package org.example.db;

import com.codahale.metrics.MetricRegistry;
import org.example.constants.DatabaseType;

import static org.example.constants.Constants.SQLITE_DB_PATH;

public class DatabaseFactory {
    public static Database createDatabase(DatabaseType dbType, MetricRegistry metricRegistry) {
        return switch (dbType) {
            case SQLITE -> new SQLite(SQLITE_DB_PATH, metricRegistry);
            case POSTGRES -> new Postgresql(metricRegistry);
        };
    }
}
