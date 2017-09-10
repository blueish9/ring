package org.ring.oql.statement;

import org.ring.oql.aggregate.Aggregate;
import org.ring.oql.builder.StatementBuilder;
import org.ring.oql.IQuery;
import org.ring.oql.expression.Expressible;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.ring.oql.StatementUtils.trim;

/**
 * Created by quanle on 5/10/2017.
 */
class QueryImpl implements IQuery
{
    StatementImpl iStatement;
    String select = "select ";
    String order = "order by ";
    String group = "group by ";
    String having = null;
    boolean hasGroup = false;
    boolean hasAggregate = false;

    QueryImpl(StatementImpl iStatement)
    {
        this.iStatement = iStatement;
    }

    String sort(String... fields)
    {
        String order = "";
        for (String field : fields)
        {
            order += iStatement.getColumn(field) + ",";
        }
        return trim(order);
    }

    @Override
    public void sortDescending(String... fields)
    {
        if (fields.length > 0)
        {
            order += sort(fields) + " DESC,";
        }
    }

    @Override
    public void sortAscending(String... fields)
    {
        if (fields.length > 0)
        {
            order += sort(fields) + " ASC,";
        }
    }

    @Override
    public void setProjection(String... fields)
    {

    }

  /*  void setProjection(Consumer<String> consumer, String... fields)
    {
        if (fields.length == 1 && fields[0].contains("*"))
        {
            colProjections.put("*", "*");
            for (String column : iStatement.allColumns())
            {
                select += column + ",";
                group += column + ",";

                consumer.accept(column);
            }
        }
        else
        {
            for (String field : fields)
            {
                String column = iStatement.getColumn(field);
                colProjections.put(field, column);
                select += column + ",";
                group += column + ",";

                consumer.accept(field);
            }
        }
    }*/

    @Override
    public void setAggregate(Aggregate... aggregates)
    {

    }

    void setAggregate(Consumer<String> consumer, Aggregate... aggregates)
    {
        hasAggregate = true;
        try
        {
            for (Aggregate a : aggregates)
            {
                String aggregate = a.toString(iStatement.parser);
                select += aggregate + ",";

                String alias = a.getAlias();
                if (alias != null)
                {
                    consumer.accept(alias);
                }
                else
                {
                    consumer.accept(aggregate);
                }
            }
        }
        catch (NoSuchFieldException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void groupBy(String... fields)
    {
      /*  hasGroup = true;
        if (colProjections.size() > 0 && colProjections.get("*") == null)
        {
            for (String field : fields)
            {
                if (colProjections.get(field) == null)
                {
                    group += iStatement.getColumn(field) + ",";
                }
            }
        }*/
    }

    @Override
    public void setHaving(Expressible expression)
    {
        try
        {
            String exp = expression.print(iStatement.parser);
            if (exp.startsWith("(") && exp.endsWith(")"))
            {
                exp = exp.substring(1, exp.length() - 1);
            }
            having = "having " + exp;
        }
        catch (NoSuchFieldException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void setCriteria(Expressible expression)
    {
        iStatement.setCriteria(expression);
    }

    @Override
    public String generateStatement()
    {
        return iStatement.generateStatement();
    }

    public String generateStatement(String from, String where)
    {
        if (select.equals("select "))
        {
            select += "*";
        }


        StatementBuilder builder = new StatementBuilder(trim(select), from);
        builder.setOptional(where, null);
        if (hasGroup)
        {
            builder.setOptional(trim(group), "group by ");
        }
        builder.setOptional(having, null);
        builder.setOptional(trim(order), "order by ");
        return builder.makeStatement();
    }

    @Override
    public PreparedStatement getJdbcStatement(Connection connection) throws SQLException
    {
        return iStatement.getJdbcStatement(connection);
    }

    @Override
    public List<HashMap<String, Object>> retrieveProjections()
    {
        return null;
    }

    @Override
    public <T> T execute(IExecute<T> execute) throws SQLException
    {
        return null;
    }

    @Override
    public <T> T execute(Function<ResultSet, T> function) throws SQLException
    {
        return null;
    }

    public Object getValueFromResultSet(Class<?> type, ResultSet resultSet, String fieldName) throws SQLException
    {
        if (type.equals(Integer.class))
        {
            return resultSet.getInt(fieldName);
        }

        if (type.equals((String.class)))
        {
            return resultSet.getString(fieldName);
        }

        if (type.equals(Date.class))
        {
            return resultSet.getDate(fieldName);
        }

        if (type.equals(Boolean.class))
        {
            return resultSet.getBoolean(fieldName);
        }

        if (type.equals(Float.class))
        {
            return resultSet.getFloat(fieldName);
        }

        if (type.equals(Double.class))
        {
            return resultSet.getDouble(fieldName);
        }

        if (type.equals(Byte.class))
        {
            return resultSet.getByte(fieldName);
        }

        if (type.equals(Short.class))
        {
            return resultSet.getShort(fieldName);
        }

        if (type.equals(Long.class))
        {
            return resultSet.getLong(fieldName);
        }

        return resultSet.getObject(fieldName);
    }
}