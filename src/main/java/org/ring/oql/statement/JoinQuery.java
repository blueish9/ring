package org.ring.oql.statement;

import org.ring.dml.query.EntityFactory;
import org.ring.entity.EntityManager;
import org.ring.entity.Mapper;
import org.ring.oql.IQuery;
import org.ring.oql.aggregate.Aggregate;
import org.ring.oql.builder.StatementBuilder;
import org.ring.oql.expression.Expressible;
import org.ring.oql.parser.JoinParser;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by quanle on 4/29/2017.
 */
public class JoinQuery extends JoinStatement implements IQuery
{
    private String select = "select ";
    private String order = "order by ";
    private String group = "group by ";
    private String having = null;
    private boolean hasGroup = false;
    private boolean hasAggregate = false;
    private HashMap<String, Set<String>> projections = new HashMap<>();
    private Set<String> aggregateProjections = new HashSet<>();

    public JoinQuery()
    {
        super();
    }

   /* public void setEntityProjection(String alias, String... fields)
    {
        Set<String> p = projections.get(alias);
        if (p == null)
        {
            p = new HashSet<>();
            projections.put(alias, p);
        }
        Collections.addAll(p, fields);


        String clause;
        if (fields.length == 0)
        {
            clause = alias + ".*,";
        }
        else
        {
            clause = "";
            Class<?> entity = aliasMapping.get(alias);
            for (String field : fields)
            {
                clause += alias + "." + EntityManager.getColumn(entity, field) + ",";
            }
        }
        select += clause;
    }*/

    public void setProjection(String... fields)
    {
        if (fields.length == 1)
        {
            if (fields[0].equals("*"))
            {
                for (String column : allColumns())
                {
                    select += String.format("%s as %s,", column, column.replace(".", "_"));
                    group += column + ",";

                    addProjection(column);
                }
            }
            else
            {
                String[] parts = fields[0].split(Pattern.quote("."));
                if (parts[1].equals("*"))
                {
                    String alias = parts[0];
                    Class<?> type = aliasMapping.get(alias);
                    Mapper mapper = EntityManager.getMapper(type);
                    for (String column : mapper.getTableColumns())
                    {
                        String projection = alias + "." + column;
                        select += String.format("%s as %s_%s,", projection, alias, column);
                        group += projection + ",";

                        addProjection(alias, column);
                    }
                }
            }
        }
        else
        {
            for (String field : fields)
            {
                if (field.contains("*"))
                {
                    String[] parts = fields[0].split(Pattern.quote("."));
                    if (parts[1].equals("*"))
                    {
                        String alias = parts[0];
                        Class<?> type = aliasMapping.get(alias);
                        Mapper mapper = EntityManager.getMapper(type);
                        for (String column : mapper.getTableColumns())
                        {
                            String projection = alias + "." + column;
                            select += String.format("%s as %s_%s,", projection, alias, column);
                            group += projection + ",";

                            addProjection(alias, column);
                        }
                    }
                }
                else
                {
                    String column = getColumn(field);
                    String fieldAlias = column.replace(".", "_");
                    select += String.format("%s as %s,", column, fieldAlias);
                    group += column + ",";

                    addProjection(column);
                }
            }
        }
      /*  setProjection(field ->
        {
            String[] parts = field.split(Pattern.quote("."));
            if (parts.length == 2)
            {
                String alias = parts[0];
                String fieldName = parts[1];
                ArrayList<String> p = projections.get(alias);
                if (p == null)
                {
                    p = new ArrayList<>();
                    projections.put(alias, p);
                }
                p.add(fieldName);
            }
        }, fields);*/
    }

    private void addProjection(String field)
    {
        String[] parts = field.split(Pattern.quote("."));
        if (parts.length == 2)
        {
            String alias = parts[0];
            String fieldName = parts[1];
            addProjection(alias, fieldName);
        }
    }

    private void addProjection(String alias, String fieldName)
    {
        Set<String> p = projections.get(alias);
        if (p == null)
        {
            p = new HashSet<>();
            projections.put(alias, p);
        }
        p.add(fieldName);
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
                    aggregateProjections.add(alias);
                }
                else
                {
                    aggregateProjections.add(aggregate);
                }
            }
        }
        catch (NoSuchFieldException e)
        {
            e.printStackTrace();
        }
    }

    public void groupBy(String... fields)
    {
        hasGroup = true;
        if (projections.size() > 0)
        {
            for (String field : fields)
            {
                String[] parts = field.split(Pattern.quote("."));
                String alias = parts[0];
                Set<String> fieldProjections = projections.get(alias);
                if (!fieldProjections.contains(field))
                {
                    group += getColumn(field) + ",";
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

    @Override
    public String generateStatement()
    {
        if (select.equals("select "))
        {
            setProjection("*");
        }


        StatementBuilder builder = new StatementBuilder(trim(select), trim(from));
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

    private HashMap<String, Set<String>> getProjections()
    {
        if (projections.size() < 1 && aggregateProjections.size() < 1)
        {
            HashMap<String, Set<String>> fieldProjections = new HashMap<>();
            aliasMapping.forEach((alias, type) ->
            {
                Mapper mapper = EntityManager.getMapper(type);
                fieldProjections.put(alias, mapper.getTableColumns());
            });
            return fieldProjections;
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
            try
            {
                HashMap<String, Set<String>> projections = getProjections();
                while (resultSet.next())
                {
                    HashMap<String, Object> record = new HashMap<>();
                    projections.forEach((alias, fieldProjections) ->
                    {
                        try
                        {
                            for (String fieldName : fieldProjections)
                            {
                                String field = alias + "." + fieldName;
                                String projectionAlias = alias + "_" + fieldName;
                                record.put(field, resultSet.getObject(projectionAlias));
                            }
                        }
                        catch (SQLException e)
                        {
                            e.printStackTrace();
                        }
                    });

                    for (String aggregate : aggregateProjections)
                    {
                        record.put(aggregate, resultSet.getObject(aggregate));
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

    public List<HashMap<String, Object>> retrieve() throws SQLException
    {
        if (hasAggregate)
        {
            System.err.print("Can't retrieve with aggregate");
            return null;
        }

        return execute(resultSet ->
        {
            List<HashMap<String, Object>> result = new ArrayList<>();
            try
            {
                HashMap<String, Set<String>> fieldProjections = getProjections();
                while (resultSet.next())
                {
                    HashMap<String, Object> record = new HashMap<>();
                    fieldProjections.forEach((alias, fields) ->
                    {
                        Object instance = null;
                        try
                        {
                            Mapper mapper = EntityManager.getMapper(aliasMapping.get(alias));
                            instance = EntityFactory.createEntity(mapper, resultSet, alias, fields);
                            record.put(alias, instance);
                        }
                        catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | SQLException | InstantiationException e)
                        {
                            e.printStackTrace();
                        }
                    });
                    result.add(record);
                }

                for (HashMap<String, Object> record : result)
                {
                    HashMap<String, String> joinList = ((JoinParser) parser).getListJoin();
                    for (Map.Entry<String, String> entry : joinList.entrySet())
                    {
                        String[] keys = entry.getKey().split(Pattern.quote("."));
                        String[] values = entry.getValue().split(Pattern.quote("."));

                        String aliasT1 = "";
                        String fieldName = "";
                        String aliasT2 = "";

                        if (keys.length == 2)
                        {
                            aliasT1 = keys[0];
                            fieldName = keys[1];
                            aliasT2 = values[0];
                        }
                        else if (values.length == 2)
                        {
                            aliasT1 = values[0];
                            fieldName = values[1];
                            aliasT2 = keys[0];
                        }

                        if (!aliasT1.isEmpty() && !aliasT2.isEmpty() && !fieldName.isEmpty())
                        {
                            Field field = aliasMapping.get(aliasT1).getDeclaredField(fieldName);
                            field.setAccessible(true);
                            field.set(record.get(aliasT1), record.get(aliasT2));
                          /*  Object member = record.get(aliasT2);
                            if (member instanceof List)
                            {
                                List collection = (List) member;
                                if (collection.size() == 1)
                                {
                                    field.set(record.get(aliasT1), collection.get(0));
                                }
                            }
                            else
                            {
                                field.set(record.get(aliasT1), member);
                            }*/
                        }
                    }
                }
            }
            catch (IllegalAccessException | SQLException | NoSuchFieldException e)
            {
                e.printStackTrace();
            }
            return result;
        });
    }
}
