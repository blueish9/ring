package org.ring.dao;

import org.ring.dml.Dml;
import org.ring.dml.query.EntityFactory;
import org.ring.dml.transaction.DmlType;
import org.ring.entity.EntityManager;
import org.ring.entity.Mapper;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by quanle on 6/1/2017.
 */
public class Dao<T> implements IDao<T>
{
    private Mapper mapper;

    public Dao(Class<T> entity)
    {
        mapper = EntityManager.getMapper(entity);
    }

    @Override
    public T get(Object id)
    {
        try
        {
            return (T) EntityFactory.getEntity(mapper, id);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Object save(T data)
    {
       return Dml.save(data, DmlType.Insert);
    }

    @Override
    public boolean update(T data)
    {
        return Dml.save(data, DmlType.Update) != null;
    }

    @Override
    public boolean delete(T data)
    {
      return Dml.delete(data);
    }

    @Override
    public List<T> getList()
    {
        return EntityFactory.getEntities(mapper);
    }

  /*  T createInstance(ResultSet rs) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, SQLException
    {
        Constructor<T> constructor = domainClass.getConstructor();     // no such method exception
        T data = constructor.newInstance();        // illegal access exception
        Field[] fields = mapper.getColumns();
        for (Field field : fields)
        {
            Column column = field.getAnnotation(Column.class);
            Object r = rs.getObject(column.name());
            field.invoke(data, r);
        }
        return data;
    }


    public T get(Object id)
    {
        return (T) EntityFactory.getEntities(mapper, id);
        //return queryObject(select, id);
    }


    public ArrayList<T> getList()
    {
        return queryList("");
    }


    public ArrayList<T> getList(Sort order)
    {
        if (order == Sort.ASC)
        {
            return queryList("order by ASC");
        }
        return queryList("order by DESC");
    }


    public ArrayList<T> getList(int limit)
    {
        return queryList("limit " + limit);
    }


    public ArrayList<T> getList(int limit, int offset)
    {
        return queryList("limit " + limit + " offset " + offset);
    }


    public ArrayList<T> getList(int limit, int offset, Sort order)
    {
        if (order == Sort.ASC)
        {
            return queryList("limit " + limit + " offset " + offset + " order by ASC");
        }
        return queryList("limit " + limit + " offset " + offset + " order by DESC");
    }

    ArrayList<T> queryList(String criteria)
    {
        try
        {
            String sql = select + criteria;
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet rs = statement.retrieve();
            ArrayList<T> dataSet = new ArrayList<T>();
            while (rs.next())
            {
                T data = createInstance(rs);
                dataSet.add(data);
            }
            return dataSet;
        }
        catch (SQLException | IllegalAccessException | NoSuchMethodException | InvocationTargetException | InstantiationException e)
        {
            e.printStackTrace();
        }
        return null;
    }*/

 /*   public boolean isExist(Object id)
    {
        return source.isExist(id);
    }*/

}
