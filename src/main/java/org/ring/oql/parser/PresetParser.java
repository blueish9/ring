package org.ring.oql.parser;

import java.util.ArrayList;

/**
 * Created by quanle on 6/17/2017.
 */
public class PresetParser extends Parser
{
    private  ArrayList<String> calledList;
    private int i;

    public PresetParser(ArrayList<String> calledList)
    {
        this.calledList = calledList;
        i = -1;
    }

    @Override
    public String parseField(String... field) throws NoSuchFieldException
    {
        i++;
        return calledList.get(i);
    }

    @Override
    public String getJoinTable()
    {
        return null;
    }
}
