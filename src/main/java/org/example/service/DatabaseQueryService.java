package org.example.service;

import com.codahale.metrics.MetricRegistry;
import org.example.db.ConnectionManager;
import org.example.db.SQLExecutor;
import org.example.viewmodel.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class DatabaseQueryService {
    private final ConnectionManager connectionManager;
    private final MetricRegistry metricRegistry;

    public DatabaseQueryService(ConnectionManager connectionManager, MetricRegistry metricRegistry) {
        this.connectionManager = connectionManager;
        this.metricRegistry = metricRegistry;
    }

    public Optional<List<MaxSalaryWorker>> findMaxSalaryWorker(String sqlFilePath) {
        String errorMessage = "Failed to execute findMaxSalaryWorker query";

        try (SQLExecutor executor = new SQLExecutor(connectionManager.getConnection(), metricRegistry)) {
            return executor.executeQuery(
                    sqlFilePath,
                    errorMessage,
                    rs -> new MaxSalaryWorker(
                            rs.getString("name"),
                            rs.getInt("highest_salary")
                    )
            );
        }
    }

    public Optional<List<MaxProjectCountClient>> findMaxProjectsClient(String sqlFilePath) {
        String errorMessage = "Failed to execute findMaxProjectsClient query";

        try (SQLExecutor executor = new SQLExecutor(connectionManager.getConnection(), metricRegistry)) {
            return executor.executeQuery(
                    sqlFilePath,
                    errorMessage,
                    rs -> new MaxProjectCountClient(
                            rs.getString("client_name"),
                            rs.getInt("project_count")
                    )
            );
        }
    }

    public Optional<List<ProjectPriceInfo>> printProjectPrices(String sqlFilePath) {
        String errorMessage = "Failed to execute printProjectPrices query";

        try (SQLExecutor executor = new SQLExecutor(connectionManager.getConnection(), metricRegistry)) {
            return executor.executeQuery(
                    sqlFilePath,
                    errorMessage,
                    rs -> new ProjectPriceInfo(
                            rs.getString("project_name"),
                            rs.getInt("project_price")
                    )
            );
        }
    }

    public Optional<List<LongestProject>> findLongestProject (String sqlFilePath) {
        String errorMessage = "Failed to execute findLongestProject query";

        try (SQLExecutor executor = new SQLExecutor(connectionManager.getConnection(), metricRegistry)) {
            return executor.executeQuery(
                    sqlFilePath,
                    errorMessage,
                    rs -> new LongestProject(
                            rs.getString("name"),
                            rs.getInt("duration_in_months")
                    )
            );
        }
    }

    public Optional<List<YoungestEldestWorker>> findYoungestEldestWorker(String sqlFilePath) {
        String errorMessage = "Failed to execute findYoungestEldestWorker query";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        try (SQLExecutor executor = new SQLExecutor(connectionManager.getConnection(), metricRegistry)) {
            return executor.executeQuery(
                    sqlFilePath,
                    errorMessage,
                    rs -> new YoungestEldestWorker(
                            rs.getString("type"),
                            rs.getString("name"),
                            LocalDate.parse(rs.getString("birthday"), formatter)
                    )
            );
        }
    }
}