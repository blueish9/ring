package org.ring.exporter.generator;

import org.ring.meta.annotation.entity.Id;

import java.lang.reflect.Field;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by xquan on 7/3/2017.
 */
public class SqliteGenerator extends ModelGenerator
{
    @Override
    public String getDataType(Field f)
    {
        Id id = f.getAnnotation(Id.class);
        if (id != null && id.autoGenerate())
        {
            return "integer";
        }

        Class<?> type = f.getType();
        if (type == Float.class || type == Double.class)
        {
            return "real";
        }
        if (type == Date.class || type == java.sql.Date.class || type == Time.class || type == Timestamp.class)
        {
            return "integer";
        }
        if (type == String.class)
        {
            return "text";
        }
        if (type == Boolean.class)
        {
            return "integer";
        }
        return super.getDataType(f);
    }

    @Override
    public String getAutoGenerate(Field f)
    {
        try
        {
            if (f.getAnnotation(Id.class).autoGenerate())
            {
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
        return "ID integer";
    }
}
