package org.example.service;

import com.codahale.metrics.MetricRegistry;
import org.example.db.ConnectionManager;
import org.example.mapper.ClientMapper;
import org.example.mapper.ProjectMapper;
import org.example.mapper.ProjectWorkerMapper;
import org.example.mapper.WorkerMapper;
import org.example.model.Client;
import org.example.model.Project;
import org.example.model.ProjectWorker;
import org.example.model.Worker;
import org.example.seed.Seed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class DatabasePopulateService {
    private static final Logger logger = LoggerFactory.getLogger(DatabasePopulateService.class);
    private final ConnectionManager connectionManager;
    private final MetricRegistry metricRegistry;
    private final GenericDatabaseService<Worker> workerService;
    private final GenericDatabaseService<Client> clientService;
    private final GenericDatabaseService<Project> projectService;
    private final GenericDatabaseService<ProjectWorker> projectWorkerService;

    public DatabasePopulateService(ConnectionManager connectionManager, MetricRegistry metricRegistry) {
        this.connectionManager = connectionManager;
        this.metricRegistry = metricRegistry;
        this.workerService = new GenericDatabaseService<>(connectionManager, metricRegistry);
        this.clientService = new GenericDatabaseService<>(connectionManager, metricRegistry);
        this.projectService = new GenericDatabaseService<>(connectionManager, metricRegistry);
        this.projectWorkerService = new GenericDatabaseService<>(connectionManager, metricRegistry);
    }

    /**
     * Adds a list of workers, clients, and projects to the database based on the provided statements
     */
    public void seedDatabase() {
        List<Worker> workers = Seed.workers;
        List<Client> clients = Seed.clients;
        List<Project> projects = Seed.projects;
        List<ProjectWorker> projectWorkers = Seed.projectWorkers;

        workerService.insertEntities("INSERT INTO worker (name, birthday, email, level, salary) VALUES (?, ?, ?, ?, ?) ON CONFLICT DO NOTHING", workers, new WorkerMapper());
        clientService.insertEntities("INSERT INTO client (name) VALUES (?) ON CONFLICT DO NOTHING", clients, new ClientMapper());
        projectService.insertEntities("INSERT INTO project (name, client_id, start_date, finish_date) VALUES (?, ?, ?, ?) ON CONFLICT DO NOTHING", projects, new ProjectMapper());
        projectWorkerService.insertEntities("INSERT INTO project_worker (project_id, worker_id) VALUES (?, ?) ON CONFLICT DO NOTHING", projectWorkers, new ProjectWorkerMapper());
        logger.info("Database seeding completed successfully!");
    }

    /**
     * Adds a list of workers, clients, and projects to the database based on the provided SQL file.
     */
    public void addEntities(String sqlFilePath) {
        List<Worker> workers = Seed.workers;
        List<Client> clients = Seed.clients;
        List<Project> projects = Seed.projects;
        List<ProjectWorker> projectWorkers = Seed.projectWorkers;

        try {
            Path path = Paths.get(sqlFilePath);
            String sqlContent = new String(Files.readAllBytes(path));

            String[] sqlStatements = sqlContent.split(";");

            for (String sqlStatement : sqlStatements) {
                sqlStatement = sqlStatement.trim();
                if (sqlStatement.startsWith("INSERT INTO worker")) {
                    workerService.insertEntities(sqlStatement, workers, new WorkerMapper());
                } else if (sqlStatement.startsWith("INSERT INTO client")) {
                    clientService.insertEntities(sqlStatement, clients, new ClientMapper());
                } else if (sqlStatement.startsWith("INSERT INTO project ")) {
                    projectService.insertEntities(sqlStatement, projects, new ProjectMapper());
                } else if (sqlStatement.startsWith("INSERT INTO project_worker")) {
                    projectWorkerService.insertEntities(sqlStatement, projectWorkers, new ProjectWorkerMapper());
                } else {
                    logger.warn("Unknown SQL statement: {}", sqlStatement);
                }
            }
        } catch (IOException e) {
            logger.error("Failed to read SQL file: {}", e.getMessage());
        }
    }
}