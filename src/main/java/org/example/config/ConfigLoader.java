package org.example.config;

import org.apache.commons.text.StringSubstitutor;
import org.example.constants.DatabaseType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import static org.example.constants.Constants.PROPERTIES_FILE_PATH;

public class ConfigLoader {
    private static final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);
    private Properties properties;

    public ConfigLoader() {
        properties = new Properties();
        loadFromEnvironment();
        loadFromFile(PROPERTIES_FILE_PATH);
        resolvePlaceholders();
    }

    private void loadFromEnvironment() {
        String dbUrl = System.getenv("DB_URL");
        String dbUser = System.getenv("DB_USER");
        String dbPassword = System.getenv("DB_PASSWORD");

        if (dbUrl != null) {
            properties.setProperty("db.url", dbUrl);
        }
        if (dbUser != null) {
            properties.setProperty("db.user", dbUser);
        }
        if (dbPassword != null) {
            properties.setProperty("db.password", dbPassword);
        }
    }

    private void loadFromFile(String propertiesFilePath) {
        try (InputStream input = new FileInputStream(propertiesFilePath)) {
            properties.load(input);
            logger.info("Properties loaded from file: {}", propertiesFilePath);
        } catch (IOException ex) {
            logger.error("Could not load properties from file: {}", propertiesFilePath, ex);
        }
    }

    private void resolvePlaceholders() {
        StringSubstitutor substitutor = new StringSubstitutor(System.getenv());
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String value = (String) entry.getValue();
            String resolvedValue = substitutor.replace(value);
            properties.setProperty((String) entry.getKey(), resolvedValue);
        }
        logger.info("Resolved placeholders in properties.");
    }

    public String getDbUrl(DatabaseType dbType) {
        return properties.getProperty(dbType.name().toLowerCase() + ".db.url");
    }

    public String getDbUser(DatabaseType dbType) {
        return properties.getProperty(dbType.name().toLowerCase() + ".db.user");
    }

    public String getDbPassword(DatabaseType dbType) {
        return properties.getProperty(dbType.name().toLowerCase() + ".db.password");
    }
}
