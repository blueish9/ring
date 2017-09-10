package org.ring.dml.query;

import org.ring.dml.query.converter.TypeConverter;
import org.ring.entity.Mapper;
import org.ring.meta.annotation.entity.Column;
import net.sf.cglib.proxy.Enhancer;
import org.ring.orm.OrmFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by quanle on 5/16/2017.
 */
public class EntityFactory
{
    private static TypeConverter converter = OrmFactory.getTypeConverter();

    public static Object getEntity(Mapper mapper, Object id) throws SQLException
    {
        String sql = "select * from " + mapper.getTable() + " where " + mapper.getPrimaryKey() + "=?";
        Connection connection = OrmFactory.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setObject(1, id);
        statement.setMaxRows(1);
        ResultSet rs = statement.executeQuery();
        Object data = null;
        if (rs.next())
        {
            data = createEntity(mapper, rs);
        }
        rs.close();
        statement.close();
        connection.close();
        return data;
    }

    public static ArrayList getEntities(Mapper mapper, String sql)
    {
        ArrayList list = new ArrayList();
        try
        {
            Connection connection = OrmFactory.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();
            while (rs.next())
            {
                Object data = createEntity(mapper, rs);
                list.add(data);
            }
            rs.close();
            statement.close();
            connection.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return list;
    }

    public static ArrayList getEntities(Mapper mapper)
    {
        return getEntities(mapper, "select * from " + mapper.getTable());
    }

    private static Object newInstance(Mapper mapper) throws IllegalAccessException
    {
        Object data = null;
        try
        {
            if (mapper.isLeaf())
            {
                Constructor constructor = mapper.getEntity().getConstructor();
                data = constructor.newInstance();
            }
            else
            {
                FetchInterceptor interceptor = new FetchInterceptor(mapper.getEntity());
                data = Enhancer.create(mapper.getEntity(), interceptor);
            }
        }
        catch (NoSuchMethodException | InvocationTargetException | InstantiationException e)
        {
            e.printStackTrace();
        }
        return data;
    }

    public static Object createEntity(Mapper mapper, ResultSet rs)
    {
        try
        {
            Object data = newInstance(mapper);
            for (Field field : mapper.getColumns())
            {
                Column column = field.getAnnotation(Column.class);
                Object value = rs.getObject(column.name());
                converter.setValue(data, field, value);
            }
            return data;
        }
        catch (SQLException | IllegalAccessException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T createEntity(Mapper mapper, ResultSet rs, Set<String> projections) throws SQLException, IllegalAccessException
    {
        T data = (T) newInstance(mapper);
        Class<?> entity = mapper.getEntity();
        for (String fieldName : projections)
        {
            try
            {
                Field field = entity.getDeclaredField(fieldName);
                field.setAccessible(true);
                Object value = rs.getObject(fieldName);
                converter.setValue(data, field, value);
            }
            catch (NoSuchFieldException e)
            {

            }
        }
        return data;
    }

    public static <T> T createEntity(Mapper mapper, ResultSet rs, String alias, Set<String> projections) throws IllegalAccessException, InvocationTargetException, InstantiationException, SQLException, NoSuchMethodException
    {
        T data = (T) newInstance(mapper);
        Class<?> entity = mapper.getEntity();
        for (String fieldName : projections)
        {
            try
            {
                Field field = entity.getDeclaredField(fieldName);
                field.setAccessible(true);
                Object value = rs.getObject(alias + "_" + fieldName);
                converter.setValue(data, field, value);
            }
            catch (NoSuchFieldException e)
            {

            }
        }
        return data;
    }

    public static List createEntities(Mapper mapper, ResultSet rs) throws SQLException
    {
        List list = new ArrayList();
        while (rs.next())
        {
            Object data = createEntity(mapper, rs);
            list.add(data);
        }
        return list;
    }

    public static <T> List<T> createEntities(Mapper mapper, ResultSet rs, Set<String> projections) throws SQLException, IllegalAccessException
    {
        List<T> list = new ArrayList<>();
        while (rs.next())
        {
            T data = createEntity(mapper, rs, projections);
            list.add(data);
        }
        return list;
    }
}





    /*public static Object  createEntity(Mapper mapper, ResultSet rs) throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, SQLException
    {
        Handler interceptor = new Handler(mapper.getEntities());
        //Constructor<?> constructor =  mapper.getEntities().getConstructor();     // no such method exception
        //Object data = constructor.newInstance();        // illegal access exception
        ProxyFactory factory = new ProxyFactory();
        factory.setSuperclass(mapper.getEntities());
        Object data = factory.createClass().newInstance();
        Field[] fields = mapper.getColumns();
        for (Field field : fields)
        {
            Column column = field.getAnnotation(Column.class);
            Object r = rs.getObject(column.name());
            field.invoke(data, r);
        }
        ((ProxyObject)data).setHandler(interceptor);
        return data;
    }*/