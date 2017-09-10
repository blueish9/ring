package org.ring.oql.builder;

/**
 * Created by quanle on 6/11/2017.
 */
class Clause
{
    private String condition;
    private String value;

    Clause(String value, String condition)
    {
        this.condition = condition;
        this.value = value;
    }

    public String getValue()
    {
        return value;
    }

    public boolean invalid()
    {
        return value.equals(condition);
    }
}
