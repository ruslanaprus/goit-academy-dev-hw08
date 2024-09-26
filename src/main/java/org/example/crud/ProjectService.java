package org.example.crud;

import com.codahale.metrics.MetricRegistry;
import org.example.db.ConnectionManager;
import org.example.mapper.*;
import org.example.model.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;

import static org.example.constants.Constants.*;

public class ProjectService extends AbstractGenericService<Project> {
    private static final Logger logger = LoggerFactory.getLogger(ProjectService.class);
    private final ProjectMapper projectMapper;

    public ProjectService(ConnectionManager connectionManager, MetricRegistry metricRegistry) {
        super(connectionManager, metricRegistry);
        projectMapper = new ProjectMapper();
    }

    @Override
    protected String getInsertSQL() {
        return INSERT_INTO_PROJECTS;
    }

    @Override
    protected PreparedStatement prepareCreateStatement(PreparedStatement ps, Project project) throws SQLException {
        try {
            validateProjectFields(project.getName(), project.getStart_date());
            projectMapper.mapToStatement(ps, project);
        } catch (IllegalArgumentException e) {
            logger.error("Project validation failed: {}", e.getMessage());
            throw new SQLException("Project validation failed: " + e.getMessage());
        }
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

    private void validateProjectFields(String name, LocalDate startDate) {
        try {
            if (name == null || name.length() < 2 || name.length() > 1000) {
                throw new IllegalArgumentException("Project name must be between 2 and 1000 characters.");
            }
            if (startDate == null) {
                throw new IllegalArgumentException("Project start date cannot be null.");
            }
        } catch (IllegalArgumentException e) {
            logger.error("Validation error: {}", e.getMessage());
            throw e;
        }
    }
}