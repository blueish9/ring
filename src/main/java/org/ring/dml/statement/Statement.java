package org.ring.dml.statement;

import org.ring.dml.query.EntityFactory;
import org.ring.dml.statement.interceptor.GetterInterceptor;
import org.ring.dml.statement.interceptor.SetterInterceptor;
import org.ring.entity.Mapper;
import net.sf.cglib.proxy.Enhancer;
import org.ring.oql.expression.Expressible;
import org.ring.oql.parser.Parser;
import org.ring.oql.parser.PresetParser;
import org.ring.orm.OrmFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.*;

/**
 * Created by quanle on 6/17/2017.
 */
public class Statement<T>
{
    private Expressible whereExpression;
    private Mapper mapper;
    private GetterInterceptor whereInterceptor;

    public Statement(Mapper mapper)
    {
        this.mapper = mapper;
        whereExpression = null;
    }

    public Statement(Mapper mapper, Expressible whereExpression, GetterInterceptor interceptor)
    {
        this.whereExpression = whereExpression;
        this.mapper = mapper;
        this.whereInterceptor = interceptor;
    }

    private <R> R execute(GetSql getSql, ArrayList[] parameters, GetResult<R> getResult) throws SQLException, NoSuchFieldException, IllegalAccessException
    {
        String sql = getSql.invoke();
        System.out.println(sql);

        Connection connection = OrmFactory.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);

        int i = 1;
        for (ArrayList list : parameters)
        {
            for (Object value : list)
            {
                statement.setObject(i, value);
                i++;
            }
        }

        R result = getResult.invoke(statement);

        statement.close();
        connection.close();
        return result;
    }

    private interface GetSql
    {
        String invoke() throws NoSuchFieldException;
    }

    private interface GetResult<R>
    {
        R invoke(PreparedStatement statement) throws SQLException, IllegalAccessException;
    }

    public int delete()
    {
        try
        {
            if (whereExpression == null)
            {
                return execute(() ->
                                String.format("delete from %s", mapper.getTable()),
                        new ArrayList[]{},
                        PreparedStatement::executeUpdate);
            }

            Parser parser = new PresetParser(whereInterceptor.getCalledList());
            return execute(() ->
                    {
                        String where = whereExpression.print(parser);
                        return String.format("delete from %s where %s", mapper.getTable(), where);
                    },
                    new ArrayList[]{parser.getValues()},
                    PreparedStatement::executeUpdate);
           /* {
                generator generator = new PresetParser(whereInterceptor.getCalledList());
                String where = whereExpression.print(generator);
                String sql = String.format("delete from %s where %s", mapper.getTable(), where);
            }
            Connection connection = OrmFactory.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
            int i = 1;
            for (Object value : generator.getValues())
            {
                statement.setObject(i, value);
                i++;
            }
            int rows = statement.executeUpdate();
            statement.close();
            connection.close();
            return rows;*/
        }
        catch (SQLException | NoSuchFieldException | IllegalAccessException e)
        {
            e.printStackTrace();
        }
        return 0;
    }

    public int update(Consumer<T> setValue)
    {
        SetterInterceptor setterInterceptor = new SetterInterceptor(mapper);
        T prototype = (T) Enhancer.create(mapper.getEntity(), setterInterceptor);
        setValue.accept(prototype);
        try
        {
            String set = "";
            ArrayList values = new ArrayList();
            HashMap<String, Object> valuesMap = setterInterceptor.getValueMap();
            for (Map.Entry<String, Object> entry : valuesMap.entrySet())
            {
                set += entry.getKey() + " = ?,";
                values.add(entry.getValue());
            }

            final String setValues = set.substring(0, set.length() - 1);
            if (whereExpression == null)
            {
                return execute(() ->
                                String.format("update %s set %s", mapper.getTable(), setValues),
                        new ArrayList[]{values},
                        PreparedStatement::executeUpdate);
            }
            Parser whereParser = new PresetParser(whereInterceptor.getCalledList());
            return execute(() ->
                    {
                        String where = whereExpression.print(whereParser);
                        return String.format("update %s set %s where %s", mapper.getTable(), setValues, where);
                    },
                    new ArrayList[]{values, whereParser.getValues()},
                    PreparedStatement::executeUpdate);
        /*    String where = whereExpression.print(whereParser);

            Connection connection = OrmFactory.getConnection();
            String dml = String.format("update %s set %s where %s",
                    mapper.getTable(), set.substring(0, set.length() - 1), where);
            PreparedStatement statement = connection.prepareStatement(dml);
            int i = 1;
            for (Object data : values)
            {
                statement.setObject(i, data);
                i++;
            }
            for (Object data : whereParser.getValues())
            {
                statement.setObject(i, data);
                i++;
            }
            int rows = statement.executeUpdate();
            statement.close();
            connection.close();
            return rows;*/
        }
        catch (SQLException | NoSuchFieldException | IllegalAccessException e)
        {
            e.printStackTrace();
        }
        return 0;
    }


    public List<T> query(Consumer<T> getColumn)
    {
        GetterInterceptor getterInterceptor = new GetterInterceptor(mapper);
        T prototype = (T) Enhancer.create(mapper.getEntity(), getterInterceptor);
        getColumn.accept(prototype);
        String projection = "";
        for (String column : getterInterceptor.getCalledList())
        {
            projection += column + ",";
        }
        if (projection.endsWith(","))
        {
            projection = projection.substring(0, projection.length() - 1);
        }
        return executeQuery(projection);
    }

    public List<T> query()
    {
        return executeQuery("*");
         /*   generator generator = new PresetParser(whereInterceptor.getCalledList());
            String sql = executeQuery(generator, "*");
            Connection connection = OrmFactory.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
            int i = 1;
            for (Object value : generator.getValues())
            {
                statement.setObject(i, value);
                i++;
            }
            ResultSet rs = statement.executeQuery();
            List list = EntityFactory.createEntities(mapper, rs);

            rs.close();
            statement.close();
            connection.close();

            return list;*/
    }

    private List<T> executeQuery(String projection)
    {
        try
        {
            if (whereExpression == null)
            {
                return this.<List<T>>execute(() -> String.format("select %s from %s", projection, mapper.getTable()),
                        new ArrayList[]{},
                        statement ->
                        {
                            ResultSet rs = statement.executeQuery();
                            return EntityFactory.createEntities(mapper, rs);
                        });
            }

            Parser parser = new PresetParser(whereInterceptor.getCalledList());
            String where = whereExpression.print(parser);
            return this.<List<T>>execute(() -> String.format("select %s from %s where %s", projection, mapper.getTable(), where),
                    new ArrayList[]{parser.getValues()},
                    statement ->
                    {
                        ResultSet rs = statement.executeQuery();
                        return EntityFactory.createEntities(mapper, rs);
                    });
                /*  generator generator = new PresetParser(whereInterceptor.getCalledList());
            String where = whereExpression.print(generator);
            String sql = String.format("select %s from %s where %s", projection, mapper.getTable(), where);
            Connection connection = OrmFactory.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
            int i = 1;
            for (Object value : generator.getValues())
            {
                statement.setObject(i, value);
                i++;
            }
            ResultSet rs = statement.executeQuery();
            List list = EntityFactory.createEntities(mapper, rs);

            rs.close();
            statement.close();
            connection.close();

            return list;*/
        }
        catch (IllegalAccessException | SQLException | NoSuchFieldException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}

 /*   public SelectStatement setAggregate(BiFunction<T, Aggregator, Aggregator[]> getColumn)
    {
        GetterInterceptor getterInterceptor = new GetterInterceptor(mapper);
        T prototype = (T) Enhancer.create(mapper.getEntity(), getterInterceptor);
        Aggregator aggregator = new Aggregator();
        getColumn.apply(prototype, aggregator);
        return new SelectStatement(whereExpression, mapper, )
    }

  public GroupStatement groupBy(Consumer<T> consumer)
  {

  }*/
