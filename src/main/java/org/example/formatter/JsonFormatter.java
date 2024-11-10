package org.example.formatter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.sun.net.httpserver.HttpExchange;
import org.example.mapper.json.JsonEntityMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class JsonFormatter {
    private static final Logger logger = LoggerFactory.getLogger(JsonFormatter.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final JavaTimeModule module = new JavaTimeModule();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public JsonFormatter() {
        module.addSerializer(LocalDate.class, new LocalDateSerializer(formatter));
        objectMapper.registerModule(module);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    // Generic method to convert JSON to an object using the appropriate JsonEntityMapper
    public <T> T jsonToObject(HttpExchange exchange, JsonEntityMapper<T> mapper) {
        logger.info("Converting JSON from HTTP exchange to object");
        String json;
        try (Scanner scanner = new Scanner(exchange.getRequestBody(), StandardCharsets.UTF_8)) {
            json = scanner.useDelimiter("\\A").next();
            logger.debug("Received JSON: {}", json);
            return mapper.fromJson(json);
        } catch (Exception e) {
            logger.error("Failed to convert JSON from HTTP exchange to object", e);
            throw new RuntimeException("Error parsing JSON request", e);
        }
    }

    // Generic method to convert an object to JSON using the appropriate JsonEntityMapper
    public <T> String objectToJson(T obj, JsonEntityMapper<T> mapper) {
        logger.info("Converting object to JSON using custom mapper: {}", obj);
        try {
            String json = mapper.toJson(obj);
            logger.debug("Converted object to JSON: {}", json);
            return json;
        } catch (Exception e) {
            logger.error("Failed to convert object to JSON using custom mapper", e);
            throw new RuntimeException("Error serializing object to JSON", e);
        }
    }

    // Convert an object to JSON (for single objects)
    public <T> String objectToJson(T obj) {
        logger.info("Converting object to JSON: {}", obj);
        try {
            String json = objectMapper.writeValueAsString(obj);
            logger.debug("Converted object to JSON: {}", json);
            return json;
        } catch (JsonProcessingException e) {
            logger.error("Failed to convert object to JSON", e);
            throw new RuntimeException("Failed to convert object to JSON", e);
        }
    }

    // Convert JSON to an object of the given class
    public <T> T jsonToObject(HttpExchange exchange, Class<T> clazz) {
        logger.info("Converting JSON from HTTP exchange to object of type {}", clazz.getSimpleName());
        try {
            T obj = objectMapper.readValue(exchange.getRequestBody(), clazz);
            logger.debug("Converted JSON to object: {}", obj);
            return obj;
        } catch (IOException e) {
            logger.error("Failed to convert JSON to object of type {}", clazz.getSimpleName(), e);
            throw new RuntimeException("Failed to convert JSON to object", e);
        }
    }
}