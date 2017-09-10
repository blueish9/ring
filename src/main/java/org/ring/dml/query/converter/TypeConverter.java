package org.ring.dml.query.converter;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by quanle on 6/19/2017.
 */
public interface TypeConverter
{
    void setValue(Object target, Field field, Object value) throws IllegalAccessException;
}
