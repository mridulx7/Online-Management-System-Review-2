package com.eventmgmt.dao;

import java.util.List;

// Generics-based DAO interface
public interface GenericDAO<T> {
    boolean insert(T t);
    boolean update(T t);
    boolean delete(int id);
    T getById(int id);
    List<T> getAll();
}
