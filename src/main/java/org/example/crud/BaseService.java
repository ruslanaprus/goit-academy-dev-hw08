package org.example.crud;

import org.example.mapper.json.JsonEntityMapper;

import java.util.List;
import java.util.Optional;

public interface BaseService<T> {
    String getContextPath();
    JsonEntityMapper getJsonEntityMapper();
    Optional<T> create(T entity);
    Optional<T> getById(long id);
    Optional<List<T>> listAll();
    Optional<Long> setName(long id, String name);
    Optional<Long> deleteById(long id);
}