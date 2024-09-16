package org.example.db;

import org.example.config.ConfigLoader;

import javax.sql.DataSource;

public interface Database {
    DataSource createDataSource(ConfigLoader configLoader);
}