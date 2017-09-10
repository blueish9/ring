package org.ring.exporter.generator;

import org.ring.meta.annotation.entity.Id;

import java.lang.reflect.Field;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by xquan on 7/3/2017.
 */
public class MysqlGenerator extends ModelGenerator
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
            return "decimal(13,3)";
        }
        if (type == Double.class)
        {
            return "decimal(35,5)";
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
        return super.getDataType(f);
    }

    @Override
    public String getAutoGenerate(Field f) {
        try{
            if(f.getAnnotation(Id.class).autoGenerate()){
                return "auto_increment";
            }else{
                return "";
            }
        }catch (Exception e){
            return "";
        }
    }

    @Override
    public String getExtraTablePK() {
        return "ID int auto_increment";
    }
}
