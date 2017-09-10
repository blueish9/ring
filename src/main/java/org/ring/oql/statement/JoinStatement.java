package org.ring.oql.statement;

import org.ring.entity.EntityManager;
import org.ring.entity.Mapper;
import org.ring.oql.parser.JoinParser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by quanle on 5/9/2017.
 */
abstract class JoinStatement extends StatementImpl
{
    HashMap<String, Class<?>> aliasMapping;

    JoinStatement()
    {
        aliasMapping = new HashMap<>();
        parser = new JoinParser(aliasMapping);
    }

    public void setEntity(Class<?> entity, String alias)
    {
        // check and throw aliasMapping exception
        // aliasMapping must be unique
        // aliasMapping must follow standard naming convention
        Class<?> type = EntityManager.original(entity);
        aliasMapping.put(alias, type);
        from += EntityManager.getTable(type) + " " + alias + ",";
    }

    @Override
    public String getColumn(String field)
    {
        String[] parts = field.split("\\.");
        if (parts.length == 2)
        {
            String alias = parts[0];
            field = parts[1];
            Class<?> entity = aliasMapping.get(alias);
            return alias + "." + EntityManager.getColumn(entity, field);
        }
        return null;
    }

    @Override
    public Set<String> allColumns()
    {
        Set<String> columns = new HashSet<>();
        aliasMapping.forEach((alias, type) ->
        {
            Mapper mapper = EntityManager.getMapper(type);
            for (String c : mapper.getTableColumns())
            {
                columns.add(alias + "." + c);
            }
        });
        return columns;
    }
}
