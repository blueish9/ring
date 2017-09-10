package org.ring.dml.query.converter;

import java.lang.reflect.Field;

/**
 * Created by quanle on 7/2/2017.
 */
public class DefaultConverter implements TypeConverter
{
    @Override
    public void setValue(Object target, Field field, Object value) throws IllegalAccessException
    {
        field.set(target, value);
    }
}
