package org.example.mapper;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface EntityMapper<T> {
    void mapToStatement(PreparedStatement statement, T entity) throws SQLException;
}
