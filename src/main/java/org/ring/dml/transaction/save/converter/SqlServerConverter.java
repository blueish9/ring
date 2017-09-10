package org.ring.dml.transaction.save.converter;

import java.math.BigDecimal;

/**
 * Created by quanle on 6/5/2017.
 */
public class SqlServerConverter extends IdConverter<BigDecimal>
{
  /*  @Override
    public void setId(Object target, Field idField, Object id) throws IllegalAccessException
    {
        if (id instanceof BigInteger)
        {
            Class<?> type = idField.getType();
            if (type == Integer.class)
            {
                int newId = ((BigInteger) id).intValue();
                idField.set(target, newId);
            }
            else
            {
                if (type == Long.class)
                {
                    long newId = ((BigInteger) id).longValue();
                    idField.set(target, newId);
                }
            }
        }
    }*/
}
