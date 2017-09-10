package org.ring.dml.query.converter;

import java.lang.reflect.Field;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by quanle on 6/19/2017.
 */
public class SqliteConverter implements TypeConverter
{
    @Override
    public void setValue(Object target, Field field, Object value) throws IllegalAccessException
    {
        if (value == null)
        {
            field.set(target, null);
            return;
        }

        if (field.getType() == Date.class)
        {
            if (value instanceof Long)
            {
                Date date = new Date((long) value);
                field.set(target, date);
                return;
            }
            if (value instanceof String)
            {
                long m = Long.parseUnsignedLong((String)value);
                Date date = new Date(m);
                field.set(target, date);
                return;
            }
        }

        if (field.getType() == java.sql.Date.class)
        {
            if (value instanceof Long)
            {
                java.sql.Date date = new java.sql.Date((long) value);
                field.set(target, date);
                return;
            }
            if (value instanceof String)
            {
                long m = Long.parseUnsignedLong((String)value);
                java.sql.Date date = new java.sql.Date(m);
                field.set(target, date);
                return;
            }
        }

        if (field.getType() == Time.class)
        {
            if (value instanceof Long)
            {
                Time time = new Time((long) value);
                field.set(target, time);
                return;
            }
            if (value instanceof String)
            {
                long m = Long.parseUnsignedLong((String)value);
                Time time = new Time(m);
                field.set(target, time);
                return;
            }
        }

        if (field.getType() == Timestamp.class)
        {
            if (value instanceof Long)
            {
                Timestamp time = new Timestamp((long) value);
                field.set(target, time);
                return;
            }
            if (value instanceof String)
            {
                long m = Long.parseUnsignedLong((String)value);
                Timestamp time = new Timestamp(m);
                field.set(target, time);
                return;
            }
        }

        if (value instanceof Integer)
        {
            if (field.getType() == Long.class)
            {
                long data = ((Integer)value).longValue();
                field.set(target, data);
                return;
            }
            if (field.getType() == Boolean.class)
            {
                int data = (int)value;
                switch (data)
                {
                    case 0:
                        field.set(target, false);
                        return;

                    case 1:
                        field.set(target, true);
                        return;
                }
            }
        }

        if (value instanceof Float)
        {
            if (field.getType() == Double.class)
            {
                double data = ((Float)value).doubleValue();
                field.set(target, data);
                return;
            }
        }

        field.set(target, value);
    }
}
