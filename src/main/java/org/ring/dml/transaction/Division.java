package org.ring.dml.transaction;

import org.ring.exception.InvalidDataException;

import java.sql.Connection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by quanle on 6/3/2017.
 */
public abstract class Division<T>
{
    protected Set<T> dataSet;

    protected Division()
    {
        dataSet = new HashSet<>();
    }

    public abstract boolean add(T data) throws InvalidDataException, IllegalAccessException;

    public abstract void execute(Connection connection);
}
