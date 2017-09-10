package org.ring.oql.criteria;

/**
 * Created by quanle on 5/1/2017.
 */
public class Comparator
{
    protected Object value;
    private String operator;

    Comparator(){}

    public Comparator(Object value, String operator)
    {
        this.value = value;
        this.operator = operator;
    }

    public String getString()
    {
        return operator + "?";
    }

    public Object getValue()
    {
        return value;
    }
}
