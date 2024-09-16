package org.example;

import com.codahale.metrics.MetricRegistry;
import org.example.constants.DatabaseType;
import org.example.db.ConnectionManager;
import org.example.db.Database;
import org.example.db.Postgresql;
import org.example.db.SQLite;
import org.example.log.MetricsLogger;
import org.example.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.example.constants.Constants.*;

public class AppLauncher {
    private static final Logger logger = LoggerFactory.getLogger(AppLauncher.class);

    public static void main(String[] args) {
        MetricRegistry metricRegistry = new MetricRegistry();
        MetricsLogger.startLogging(metricRegistry);

        runDatabaseOperations(DatabaseType.POSTGRES, metricRegistry);
    }

    private static void runDatabaseOperations(DatabaseType dbType, MetricRegistry metricRegistry) {
        Database database;
        String initSql, populateSql, maxSalarySql, maxProjectClientSql, projectPricesSql, longestProjectSql, youngestEldestSql, dropTables;

        database = switch (dbType) {
            case SQLITE -> new SQLite(SQLITE_DB_PATH, metricRegistry);
            case POSTGRES -> new Postgresql(metricRegistry);
        };

        ConnectionManager connectionManager = ConnectionManager.getInstance(database, metricRegistry);

        // Generate SQL file paths based on the database type
        initSql = getSqlFilePath(dbType, INIT_DB_SQL);
        populateSql = getSqlFilePath(dbType, POPULATE_DB_SQL);
        maxSalarySql = getSqlFilePath(dbType, FIND_MAX_SALARY_WORKER_SQL);
        maxProjectClientSql = getSqlFilePath(dbType, FIND_MAX_PROJECT_CLIENT_SQL);
        projectPricesSql = getSqlFilePath(dbType, PRINT_PROJECT_PRICES_SQL);
        longestProjectSql = getSqlFilePath(dbType, FIND_LONGEST_PROJECT_SQL);
        youngestEldestSql = getSqlFilePath(dbType, FIND_YOUNGEST_ELDEST_SQL);
        dropTables = getSqlFilePath(dbType, DROP_TABLES_SQL);

        // Query operations
        DatabaseQueryService queryService = DatabaseServiceFactory.createDatabaseQueryService(connectionManager, metricRegistry);

        queryService.findMaxSalaryWorker(maxSalarySql).ifPresent(workers -> {
            logger.info("MaxSalaryWorker(s) found: {}", workers.size());
            workers.forEach(worker -> logger.info(worker.toString()));
        });

        queryService.findMaxProjectsClient(maxProjectClientSql).ifPresent(clients -> {
            logger.info("MaxProjectCountClient(s) found: {}", clients.size());
            clients.forEach(client -> logger.info(client.toString()));
        });

        queryService.printProjectPrices(projectPricesSql).ifPresent(projects -> {
            logger.info("ProjectPriceInfo(s) found: {}", projects.size());
            projects.forEach(project -> logger.info(project.toString()));
        });

        queryService.findLongestProject(longestProjectSql).ifPresent(projects -> {
            logger.info("LongestProject(s) found: {}", projects.size());
            projects.forEach(project -> logger.info(project.toString()));
        });

        queryService.findYoungestEldestWorker(youngestEldestSql).ifPresent(workers -> {
            logger.info("YoungestEldestWorker(s) found: {}", workers.size());
            workers.forEach(worker -> logger.info(worker.toString()));
        });
    }
}