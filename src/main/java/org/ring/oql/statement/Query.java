package org.ring.oql.statement;

import org.ring.dml.query.EntityFactory;
import org.ring.entity.EntityManager;
import org.ring.entity.Mapper;
import org.ring.oql.IQuery;
import org.ring.oql.aggregate.Aggregate;
import org.ring.oql.builder.StatementBuilder;
import org.ring.oql.expression.Expressible;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by quanle on 4/28/2017.
 */
public class Query extends Statement implements IQuery
{
    private Set<String> projections;
    private String select = "select ";
    private String order = "order by ";
    private String group = "group by ";
    private String having = null;
    private boolean hasGroup = false;
    private boolean hasAggregate = false;

    public Query(Class<?> entity)
    {
        super(entity);
        projections = new HashSet<>();
    }

    public void setProjection(String... fields)
    {
        if (fields.length == 1 && fields[0].contains("*"))
        {
            for (String column : allColumns())
            {
                select += column + ",";
                group += column + ",";

                projections.add(column);
            }
        }
        else
        {
            for (String field : fields)
            {
                String column = getColumn(field);
                select += column + ",";
                group += column + ",";

                projections.add(column);
            }
        }
    }

    @Override
    public void setAggregate(Aggregate... aggregates)
    {
        hasAggregate = true;
        try
        {
            for (Aggregate a : aggregates)
            {
                String aggregate = a.toString(parser);
                select += aggregate + ",";

                String alias = a.getAlias();
                if (alias != null)
                {
                    projections.add(alias);
                }
                else
                {
                    projections.add(aggregate);
                }
            }
        }
        catch (NoSuchFieldException e)
        {
            e.printStackTrace();
        }
    }

    String sort(String... fields)
    {
        String order = "";
        for (String field : fields)
        {
            order += getColumn(field) + ",";
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

    public void groupBy(String... fields)
    {
        hasGroup = true;
        if (projections.size() > 0)
        {
            for (String field : fields)
            {
                String column = getColumn(field);
                if (!projections.contains(column))
                {
                    group += column + ",";
                }
            }
        }
    }

    public void setHaving(Expressible expression)
    {
        try
        {
            String exp = expression.print(parser);
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
    public String generateStatement()
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
        return getJdbcStatement(connection, parser.getValues());
    }

    private Set<String> getProjections()
    {
        if (projections.size() < 1)
        {
            return allColumns();
        }
        return projections;
    }

    @Override
    public List<HashMap<String, Object>> retrieveProjections() throws SQLException
    {
        return execute(resultSet ->
        {
            try
            {
                ResultSetMetaData metaData = resultSet.getMetaData();
                if (metaData == null)
                {

                }
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }

            List<HashMap<String, Object>> result = new ArrayList<>();
            Set<String> projections = getProjections();
            try
            {
                while (resultSet.next())
                {
                    HashMap<String, Object> record = new HashMap<>();
                    for (String fieldName : projections)
                    {
                        record.put(fieldName, resultSet.getObject(fieldName));
                    }
                    result.add(record);
                }
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
            return result;
        });
    }

    public <T> List<T> retrieve() throws SQLException
    {
        if (hasAggregate)
        {
            System.err.print("Can't retrieve with aggregate");
            return null;
        }

        // retrieve method can only be run with @Id field in projections
        Set<String> projections = getProjections();
        Mapper mapper = EntityManager.getMapper(entity);
        String id = mapper.getIdField().getName();
        String idColumn = mapper.getPrimaryKey();
        if (!projections.contains(idColumn))
        {
            setProjection(id);
        }
        return execute(resultSet ->
        {
            try
            {

                return EntityFactory.<T>createEntities(mapper, resultSet, projections);
            }
            catch (IllegalAccessException | SQLException e)
            {
                e.printStackTrace();
            }
            return null;
        });
    }
}
