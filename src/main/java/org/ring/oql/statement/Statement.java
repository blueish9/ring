package org.ring.oql.statement;

import org.ring.entity.EntityManager;
import org.ring.entity.Mapper;
import org.ring.oql.parser.SingleParser;

import java.util.Set;


/**
 * Created by quanle on 4/29/2017.
 */
abstract class Statement extends StatementImpl
{
    Class<?> entity;

    Statement(Class<?> entity)
    {
        this.entity = entity;
        from += EntityManager.getTable(entity);

        parser = new SingleParser(entity);
    }

    @Override
    public String getColumn(String field)
    {
        if (field.length() > 0 && field.charAt(0) == '.')
        {
            return field.substring(1);
        }
        return EntityManager.getColumn(entity, field);
    }

    @Override
    public Set<String> allColumns()
    {
        Mapper mapper = EntityManager.getMapper(entity);
        return mapper.getTableColumns();
    }
}
