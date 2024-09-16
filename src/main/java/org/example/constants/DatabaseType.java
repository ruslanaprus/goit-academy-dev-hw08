package org.example.constants;

import static org.example.constants.Constants.SQL_POSTGRES_DIR;
import static org.example.constants.Constants.SQL_SQLITE_DIR;

public enum DatabaseType {
    POSTGRES(SQL_POSTGRES_DIR),
    SQLITE(SQL_SQLITE_DIR);

    private final String sqlDirectory;

    DatabaseType(String sqlDirectory) {
        this.sqlDirectory = sqlDirectory;
    }

    public String getSqlDirectory() {
        return sqlDirectory;
    }
}
