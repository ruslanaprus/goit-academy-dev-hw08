package org.example.mapper.dbentity;

import org.example.model.ProjectWorker;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ProjectWorkerMapper implements EntityMapper<ProjectWorker> {

    @Override
    public void mapToStatement(PreparedStatement statement, ProjectWorker entity) throws SQLException {
        statement.setInt(1, entity.getProjectId());
        statement.setInt(2, entity.getWorkerId());
    }
}
