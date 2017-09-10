package org.ring.oql.parser;

import java.util.ArrayList;

/**
 * Created by quanle on 5/9/2017.
 */
public abstract class Parser
{
    private ArrayList values = new ArrayList();

    public void addValue(Object value)
    {
        values.add(value);
    }

    public ArrayList getValues()
    {
        return values;
    }

    public abstract String parseField(String... field) throws NoSuchFieldException;

    public abstract String getJoinTable();
}
