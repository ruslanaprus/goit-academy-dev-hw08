package org.example.formatter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.sun.net.httpserver.HttpExchange;
import org.example.mapper.JsonEntityMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class JsonFormatter {
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
        String json = new Scanner(exchange.getRequestBody(), StandardCharsets.UTF_8).useDelimiter("\\A").next();
        return mapper.fromJson(json);
    }

    // Generic method to convert an object to JSON using the appropriate JsonEntityMapper
    public <T> String objectToJson(T obj, JsonEntityMapper<T> mapper) {
        return mapper.toJson(obj);
    }

    // Convert an object to JSON (for single objects)
//    public <T> String objectToJson(T obj) {
//        try {
//            return objectMapper.writeValueAsString(obj);
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException("Failed to convert object to JSON", e);
//        }
//    }
//
//    // Convert a list of objects to JSON (for lists of objects)
//    public <T> String objectToJson(List<T> list) {
//        try {
//            return objectMapper.writeValueAsString(list);
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException("Failed to convert list of objects to JSON", e);
//        }
//    }
//
//    // Convert JSON to an object of the given class
//    public <T> T jsonToObject(HttpExchange exchange, Class<T> clazz) {
//        try {
//            return objectMapper.readValue(exchange.getRequestBody(), clazz);
//        } catch (IOException e) {
//            throw new RuntimeException("Failed to convert JSON to object", e);
//        }
//    }
}