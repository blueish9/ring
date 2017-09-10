package org.ring.dml.transaction.save.converter;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

/**
 * Created by quanle on 6/5/2017.
 */
public class IdConverter<T extends Number>
{
    protected Class<T> numberType;

    public IdConverter()
    {
        ParameterizedType pt = (ParameterizedType) getClass().getGenericSuperclass();
        numberType = (Class<T>) pt.getActualTypeArguments()[0];
    }

    public boolean setId(Object target, Field idField, Object id) throws IllegalAccessException
    {
        if (id.getClass() == numberType)
        {
            Class<?> type = idField.getType();
            if (type == Integer.class)
            {
                int newId = ((Number) id).intValue();
                idField.set(target, newId);
            }
            else
            {
                if (type == Long.class)
                {
                    long newId = ((Number) id).longValue();
                    idField.set(target, newId);
                }
            }
            return true;
        }
        return false;
    }
}
