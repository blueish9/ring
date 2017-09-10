package org.ring.entity;

import org.ring.exception.EntityMappingException;
import net.sf.cglib.proxy.Factory;

import java.util.HashMap;

/**
 * Created by quanle on 5/5/2017.
 */
public final class EntityManager
{
    private static HashMap<Class<?>, Mapper> entityMap = new HashMap<>();

    public static Mapper getMapper(Class<?> entity)
    {
        try
        {
            entity = original(entity);
            Mapper mapper = entityMap.get(entity);
            if (mapper == null)
            {
                mapper = new Mapper(entity);
                entityMap.put(entity, mapper);
            }
            return mapper;
        }
        catch (EntityMappingException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static String getTable(Class<?> entity)
    {
        return getMapper(entity).getTable();
    }

    public static String getColumn(Class<?> entity, String field)
    {
        return getMapper(entity).getColumn(field);
    }

    public static Class<?> original(Class<?> type)
    {
        try
        {
            type.getDeclaredField("CGLIB$BOUND");
            if (type.getSuperclass() != Object.class)
            {
                return type.getSuperclass();
            }
            for (Class<?> i : type.getInterfaces())
            {
                if (i != Factory.class)
                {
                    return i;
                }
            }
            return Object.class;
        }
        catch (NoSuchFieldException ignored)
        {
            return type;
        }
    }
}
