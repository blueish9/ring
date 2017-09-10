package org.ring.dml.transaction.save.converter;

import java.lang.reflect.Field;

/**
 * Created by quanle on 6/5/2017.
 */
public class DefaultConverter extends IdConverter<Integer>
{
    private IdConverter converter;

    public DefaultConverter()
    {
        super();
        attach(new LongConverter());
    }

    protected void attach(IdConverter converter)
    {
        this.converter = converter;
    }

    @Override
    public boolean setId(Object target, Field idField, Object id) throws IllegalAccessException
    {
        // converter.setId() will only run if super.setId() is false
        return super.setId(target, idField, id) || converter.setId(target, idField, id);
    }
}


/*   @Override
    public void setId(Object target, Field idField, Object id) throws IllegalAccessException
    {
        if (id instanceof Integer)
        {
            Class<?> type = idField.getType();
            if (type == Integer.class)
            {
                idField.set(target, id);
            }
            else
            {
                if (type == Long.class)
                {
                    long newId = ((Integer) id).longValue();
                    idField.set(target, newId);
                }
            }
        }
    }*/