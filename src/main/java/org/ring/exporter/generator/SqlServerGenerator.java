package org.ring.exporter.generator;

import org.ring.meta.annotation.entity.Id;

import java.lang.reflect.Field;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by xquan on 7/3/2017.
 */
public class SqlServerGenerator extends ModelGenerator
{
    @Override
    public String getDataType(Field f)
    {
        Class<?> type = f.getType();
        if (type == Long.class)
        {
            return "bigint";
        }
        if (type == Float.class)
        {
            return "float(24)";
        }
        if (type == Double.class)
        {
            return "float(53)";
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
            return "datetime";
        }
        if (type == Boolean.class)
        {
            return "bit";
        }
        if (type == String.class)
        {
            return "nvarchar(256)";
        }
        return super.getDataType(f);
    }

    @Override
    public String getAutoGenerate(Field f) {
        try{
            if(f.getAnnotation(Id.class).autoGenerate()){
                return "IDENTITY(1,1)";
            }else{
                return "";
            }
        }catch (Exception e){
            return "";
        }
    }

    @Override
    public String getExtraTablePK() {
        return "ID int IDENTITY(1,1)";
    }
}
