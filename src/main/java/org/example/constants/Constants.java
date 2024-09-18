package org.example.constants;

public class Constants {
    public static final String PROPERTIES_FILE_PATH = "src/main/resources/config.properties";

    // DB names
    public static final String SQLITE_DB = "bob.db";

    public static final String SQLITE_DB_PATH = DatabaseType.SQLITE.getSqlDirectory() + SQLITE_DB;

    // Base directories for SQL files
    public static final String SQL_POSTGRES_DIR = "src/main/resources/sql/postgres/";
    public static final String SQL_SQLITE_DIR = "src/main/resources/sql/sqlite/";

    // Common SQL file names
    public static final String FIND_MAX_SALARY_WORKER_SQL = "find_max_salary_worker.sql";
    public static final String FIND_LONGEST_PROJECT_SQL = "find_longest_project.sql";
    public static final String FIND_MAX_PROJECT_CLIENT_SQL = "find_max_projects_client.sql";
    public static final String FIND_YOUNGEST_ELDEST_SQL = "find_youngest_eldest_workers.sql";
    public static final String PRINT_PROJECT_PRICES_SQL = "print_project_prices.sql";
    public static final String DROP_TABLES_SQL = "drop_tables.sql";

    // Generate full SQL file path based on database type
    public static String getSqlFilePath(DatabaseType databaseType, String fileName) {
        return databaseType.getSqlDirectory() + fileName;
    }

    // CRUD for Clients table
    public static final String LIST_ALL_CLIENTS = "SELECT id, name FROM client LIMIT 50";
    public static final String INSERT_INTO_CLIENTS = "INSERT INTO client (name) VALUES (?)";
    public static final String GET_CLIENT_BY_ID = "SELECT id, name FROM client WHERE id = ?";
    public static final String SET_NEW_CLIENTS_NAME = "UPDATE client SET name = ? WHERE id = ?";
    public static final String DELETE_CLIENT_BY_ID = "DELETE FROM client WHERE id = ?";
}
