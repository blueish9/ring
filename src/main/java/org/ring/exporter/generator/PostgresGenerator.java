package org.ring.exporter.generator;

import org.ring.entity.EntityManager;
import org.ring.entity.Mapper;
import org.ring.meta.annotation.entity.Id;

import java.lang.reflect.Field;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by xquan on 7/3/2017.
 */
public class PostgresGenerator extends ModelGenerator
{

    @Override
    public String getDataType(Field f)
    {
        Id id = f.getAnnotation(Id.class);
        if (id != null && id.autoGenerate())
        {
            if (f.getType().equals(Long.class))
            {
                return "bigserial";
            }
            return "serial";
        }

        return getType(f);
    }

    @Override
    public String getAutoGenerate(Field f)
    {
        try
        {
            if (f.getAnnotation(Id.class).autoGenerate())
            {
                if (getDataType(f).equals("long"))
                {
                    return "";
                }
                return "";
            }
            else
            {
                return "";
            }
        }
        catch (Exception e)
        {
            return "";
        }
    }

    @Override
    public String getExtraTablePK() {
        return "ID serial";
    }

    @Override
    public String getForeignType(Class c) {
        Mapper mapper = EntityManager.getMapper(c);
        return getType(mapper.getIdField());
    }

    String getType(Field f) {
        Class<?> type = f.getType();
        if (type == Short.class)
            return "smallint";
        if (type == Integer.class)
            return "integer";
        if (type == Long.class)
            return "bigint";
        if (type == String.class)
            return "text";
        if (type == Integer.class)
            return "integer";

        if (type == Float.class)
        {
            return "real";
        }
        if (type == Double.class)
        {
            return "double precision";
        }
        if (type == Date.class || type == java.sql.Date.class)
        {
            return "date";
        }
        if (type == Time.class)
        {
            return "time";
        }
        if (type == Timestamp.class)
        {
            return "timestamp";
        }
        return super.getDataType(f);
    }
}
