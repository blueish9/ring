package org.ring.dml;

import org.ring.dml.transaction.DmlType;
import org.ring.dml.transaction.delete.DeleteTransaction;
import org.ring.dml.transaction.save.SaveTransaction;
import org.ring.entity.EntityManager;
import org.ring.entity.Mapper;
import org.ring.dml.statement.Statement;
import org.ring.dml.statement.interceptor.GetterInterceptor;
import net.sf.cglib.proxy.Enhancer;
import org.ring.oql.expression.Expressible;
import org.ring.orm.OrmFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Function;

/**
 * Created by quanle on 6/25/2017.
 */
public final class Dml
{
    public static <T> Statement<T> newStatement(Class<T> entity)
    {
        Mapper mapper = EntityManager.getMapper(entity);
        return new Statement<>(mapper);
    }

    public static <T> Statement<T> newStatement(Class<T> entity, Function<T, Expressible> function)
    {
        Mapper mapper = EntityManager.getMapper(entity);
        GetterInterceptor interceptor = new GetterInterceptor(mapper);
        T prototype = (T) Enhancer.create(mapper.getEntity(), interceptor);
        return new Statement<>(mapper, function.apply(prototype), interceptor);
    }

    public static Object save(Object data, DmlType dmlType)
    {
        try
        {
            Connection connection = OrmFactory.getConnection();
            connection.setAutoCommit(false);

            SaveTransaction transaction = new SaveTransaction(dmlType);
            Object id = transaction.execute(connection, data);

            connection.commit();
            connection.close();
            return id;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean delete(Object data)
    {
        try
        {
            Connection connection = OrmFactory.getConnection();
            connection.setAutoCommit(false);

            DeleteTransaction transaction = new DeleteTransaction();
            boolean result = transaction.execute(connection, data);

            connection.commit();
            connection.close();
            return result;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return false;
    }
}
