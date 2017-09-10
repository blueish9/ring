package org.ring.exporter.generator;

import org.ring.entity.EntityManager;
import org.ring.entity.Mapper;
import org.ring.meta.annotation.entity.Column;

import java.lang.reflect.Field;
import java.util.Date;

/**
 * Created by xquan on 7/3/2017.
 */
public abstract class ModelGenerator
{

    public abstract String getAutoGenerate(Field f);
    public abstract String getExtraTablePK();

    public String getDataType(Field f){
        Class<?> type = f.getType();
        if (type.equals(Integer.class)) {
            return "int";
        } else if (type.equals((String.class))) {
            return "varchar(256)";
        } else if (type.equals(Date.class)) {
            return "date";
        } else if (type.equals(Boolean.class)) {
            return "bool";
        } else if (type.equals(Float.class)) {
            return "float";
        } else if (type.equals(Double.class)) {
            return "double";
        } else if(type.equals(Byte.class)) {
            return "varbinary(10000)";
        } else if (type.equals(Short.class)) {
            return "integer";
        } else if (type.equals(Long.class)) {
            return "long";
        } else {
            return "varchar(256)";
        }
    }

    public String getNullable(Field f) {
        return f.getAnnotation(Column.class).nullable()? "" : "not null";
    }

    public String getPrimaryKey(Class c) {
        Mapper mapper = EntityManager.getMapper(c);
        return mapper.getIdField().getAnnotation(Column.class).name();
    }

    public String getForeignType(Class c) {
        Mapper mapper = EntityManager.getMapper(c);
        return getDataType(mapper.getIdField());
    }

    public String getTableName(Class c) {
        try {
            Mapper mapper = EntityManager.getMapper(c);
            return mapper.getTable();
        } catch (Exception e){
            return "";
        }
    }
}
