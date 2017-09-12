package org.ring.repository;

import org.ring.entity.EntityManager;
import org.ring.entity.Mapper;
import org.ring.dml.statement.Statement;
import org.ring.dml.statement.interceptor.GetterInterceptor;
import net.sf.cglib.proxy.Enhancer;
import org.ring.oql.expression.Expressible;

import java.util.function.Function;

/**
 * Created by quanle on 6/17/2017.
 */
public class LambdaSql<T>
{
    private Mapper mapper;

    public LambdaSql(Class<T> entity)
    {
        mapper = EntityManager.getMapper(entity);
    }

    public Statement<T> newStatement(Function<T, Expressible> function)
    {
        GetterInterceptor interceptor = new GetterInterceptor(mapper);
        T prototype = (T) Enhancer.create(mapper.getEntity(), interceptor);
        return new Statement<>(mapper, function.apply(prototype), interceptor);
    }

    public Statement<T> newStatement()
    {
        return new Statement<>(mapper);
    }

  /*  public static <S> Statement<S> newStatement(Class<S> entity, Function<S, Expressible> function)
    {
        Mapper mapper = EntityManager.getMapper(entity);
        GetterInterceptor interceptor = new GetterInterceptor(mapper);
        S prototype = (S) Enhancer.create(mapper.getEntity(), interceptor);
        return new Statement<>(mapper, function.apply(prototype), interceptor);
    }*/
}
