package org.ring.oql.parser;

import org.ring.entity.EntityManager;

/**
 * Created by quanle on 5/9/2017.
 */
public class SingleParser extends Parser
{
    Class<?> entity;

    public SingleParser(Class<?> entity)
    {
        this.entity = entity;
    }

    @Override
    public String parseField(String... field)
    {
        if (field[0].equals("*"))
        {
            return "*";
        }
        return EntityManager.getColumn(entity, field[0]);
    }

    @Override
    public String getJoinTable()
    {
        return null;
    }
}
