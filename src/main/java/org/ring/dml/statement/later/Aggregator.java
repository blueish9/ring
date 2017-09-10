package org.ring.dml.statement.later;

import java.util.ArrayList;

/**
 * Created by quanle on 6/30/2017.
 */
public class Aggregator
{
    ArrayList<String> calledList = new ArrayList<>();

    public ArrayList<String> getCalledList()
    {
        return calledList;
    }

    public Aggregator count(Object field)
    {
        calledList.add("count");
        return this;
    }

    public Aggregator max(Object field)
    {
        calledList.add("max");
        return this;
    }
}
