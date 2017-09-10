package org.ring.dao;

import java.util.List;

/**
 * Created by quanle on 6/10/2017.
 */
public interface IDao<T>
{
    Object save(T data);

    boolean update(T data);

    boolean delete(T data);

    T get(Object id);

    List<T> getList();
}