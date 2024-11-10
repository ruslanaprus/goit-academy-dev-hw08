package org.example.mapper.json;

public interface JsonEntityMapper<T> {
    T fromJson(String json);
    String toJson(T entity);
}