package org.example.crud;

import com.codahale.metrics.MetricRegistry;
import org.example.db.ConnectionManager;
import org.example.db.SQLExecutor;
import org.example.mapper.ClientJsonMapper;
import org.example.mapper.JsonEntityMapper;
import org.example.mapper.ProjectJsonMapper;
import org.example.model.Client;
import org.example.model.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.example.constants.Constants.*;

public class ProjectService extends AbstractGenericService<Project> {

    public ProjectService(ConnectionManager connectionManager, MetricRegistry metricRegistry) {
        super(connectionManager, metricRegistry);
    }

    @Override
    protected String getInsertSQL() {
        return INSERT_INTO_PROJECTS;
    }

    @Override
    protected PreparedStatement prepareCreateStatement(PreparedStatement ps, Project project) throws SQLException {
        ps.setString(1, project.getName());
        ps.setLong(2, project.getClient_id());
        ps.setDate(3, Date.valueOf(project.getStart_date()));
        ps.setDate(4, project.getFinish_date() != null ? Date.valueOf(project.getFinish_date()) : null);
        return ps;
    }

    @Override
    protected Project createEntityFromResultSet(ResultSet rs) throws SQLException {
        long projectId = rs.getLong("id");
        String name = rs.getString("name");
        long client_id = rs.getLong("client_id");
        LocalDate start_date = rs.getDate("start_date").toLocalDate();
        LocalDate finish_date = rs.getDate("finish_date").toLocalDate();
        return new Project(projectId, name, client_id, start_date, finish_date != null ? finish_date : null);
    }

    @Override
    protected String getSelectByIdSQL() {
        return GET_PROJECT_BY_ID;
    }

    @Override
    protected String getListAllSQL() {
        return LIST_ALL_PROJECTS;
    }

    @Override
    protected String getUpdateNameSQL() {
        return SET_NEW_PROJECTS_NAME;
    }

    @Override
    protected String getDeleteByIdSQL() {
        return DELETE_PROJECT_BY_ID;
    }

    @Override
    public String getContextPath() {
        return "/projects";
    }

    @Override
    public JsonEntityMapper getJsonEntityMapper() {
        return new ProjectJsonMapper();
    }
}

//public class ProjectService implements BaseService {
//    private static final Logger logger = LoggerFactory.getLogger(ProjectService.class);
//    private final ConnectionManager connectionManager;
//    private final MetricRegistry metricRegistry;
//
//    public ProjectService(ConnectionManager connectionManager, MetricRegistry metricRegistry) {
//        this.connectionManager = connectionManager;
//        this.metricRegistry = metricRegistry;
//    }
//
//    @Override
//    public String getContextPath() {
//        return "/projects";
//    }
//
//    public JsonEntityMapper getJsonEntityMapper() {
//        return new ProjectJsonMapper();
//    }
//
//    public Optional<Project> create(Project project) {
//        try {
//            validateProjectFields(project.getName(), project.getStart_date());
//            logger.info("Creating project with name {}", project.getName());
//
//            try (Connection connection = connectionManager.getConnection();
//                 PreparedStatement statement = connection.prepareStatement(INSERT_INTO_PROJECTS, Statement.RETURN_GENERATED_KEYS)) {
//
//                statement.setString(1, project.getName());
//                statement.setLong(2, project.getClient_id());
//                statement.setDate(3, Date.valueOf(project.getStart_date()));
//                statement.setDate(4, project.getFinish_date() != null ? Date.valueOf(project.getFinish_date()) : null);
//
//                int affectedRows = statement.executeUpdate();
//                logger.info("affected rows: {}", affectedRows);
//
//                if (affectedRows > 0) {
//                    try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
//                        if (generatedKeys.next()) {
//                            long generatedId = generatedKeys.getLong(1);
//                            Project createdProject = new Project(generatedId, project.getName(), project.getClient_id(), project.getStart_date(), project.getFinish_date());
//                            return Optional.of(createdProject);
//                        }
//                    }
//                } else {
//                    logger.error("Failed to insert the project, no rows affected.");
//                }
//            }
//        } catch (SQLException e) {
//            logger.error("SQL error while adding a project to the database. SQLState: {}, ErrorCode: {}", e.getSQLState(), e.getErrorCode(), e);
//        } catch (IllegalArgumentException e) {
//            logger.error("Invalid project fields: {}", e.getMessage());
//        }
//
//        return Optional.empty();
//    }
//
//    public Optional<Project> getById(long id) {
//        return new SQLExecutor(connectionManager.getConnection(), metricRegistry)
//                .executeSingleQuery(
//                        GET_PROJECT_BY_ID,
//                        "Error retrieving project by id",
//                        rs -> {
//                            long projectId = rs.getLong("id");
//                            String name = rs.getString("name");
//                            long client_id = rs.getLong("client_id");
//                            LocalDate start_date = rs.getDate("start_date").toLocalDate();
//                            LocalDate finish_date = rs.getDate("finish_date").toLocalDate();
//                            return new Project(projectId, name, client_id, start_date, finish_date != null ? finish_date : null);
//                        },
//                        ps -> {
//                            try {
//                                ps.setLong(1, id);
//                            } catch (SQLException e) {
//                                throw new RuntimeException(e);
//                            }
//                        }
//                );
//    }
//
//    public Optional<List<Project>> listAll() {
//        String errorMessage = "Failed to execute listAll projects query";
//
//        return new SQLExecutor(connectionManager.getConnection(), metricRegistry).executeQuery(
//                LIST_ALL_PROJECTS,
//                errorMessage,
//                rs -> {
//                    long id = rs.getLong("id");
//                    String name = rs.getString("name");
//                    long client_id = rs.getLong("client_id");
//                    LocalDate start_date = rs.getDate("start_date").toLocalDate();
//                    LocalDate finish_date = rs.getDate("finish_date").toLocalDate();
//                    return new Project(id, name, client_id, start_date, finish_date != null ? finish_date : null);
//                }
//        );
//    }
//
//    public Optional<Long> setName(long id, String name) {
//        try (Connection connection = connectionManager.getConnection();
//             PreparedStatement preparedStatement = connection.prepareStatement(SET_NEW_PROJECTS_NAME)) {
//
//            logger.info("Prepared statement {}", preparedStatement);
//            preparedStatement.setString(1, name);
//            preparedStatement.setLong(2, id);
//
//            int affectedRows = preparedStatement.executeUpdate();
//            if (affectedRows > 0) {
//                logger.info("Successfully updated {} row(s) for project with id {}", affectedRows, id);
//                return Optional.of(id);
//            } else {
//                logger.error("Failed to update project with id {}, no rows affected", id);
//                return Optional.empty();
//            }
//        } catch (SQLException e) {
//            logger.error("SQL error while updating project with id {}. SQLState: {}, ErrorCode: {}", id, e.getSQLState(), e.getErrorCode(), e);
//            return Optional.empty();
//        }
//    }
//
//    public Optional<Long> deleteById(long id) {
//        try (Connection connection = connectionManager.getConnection();
//             PreparedStatement preparedStatement = connection.prepareStatement(DELETE_PROJECT_BY_ID)) {
//
//            preparedStatement.setLong(1, id);
//
//            long affectedRows = preparedStatement.executeUpdate();
//            if (affectedRows > 0) {
//                logger.info("Successfully deleted {} row(s) for project with id {}", affectedRows, id);
//                return Optional.of(affectedRows);
//            } else {
//                logger.error("Failed to delete project with id {}, no rows affected", id);
//                return Optional.empty();
//            }
//        } catch (SQLException e) {
//            logger.error("SQL error while deleting project with id {}. SQLState: {}, ErrorCode: {}", id, e.getSQLState(), e.getErrorCode(), e);
//        }
//        return Optional.empty();
//    }
//
//    private void validateProjectFields(String name, LocalDate startDate) {
//        logger.info("start validation");
//        if (name == null || name.length() < 2 || name.length() > 1000) {
//            throw new IllegalArgumentException("Project name must be between 2 and 1000 characters.");
//        }
//        if (startDate == null) {
//            throw new IllegalArgumentException("Project start date cannot be null.");
//        }
//    }
//}