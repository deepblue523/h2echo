package com.deepblue523.h2echo.test.dao;

public interface INrmBaseDao<T> {
    int insert(T recToInsert);
    T getById(int id);
    int update(T recToUpdate);
    int delete(int id);
}
