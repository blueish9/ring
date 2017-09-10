package org.ring.oql.builder;

import org.ring.oql.StatementUtils;

import java.util.ArrayList;

/**
 * Created by quanle on 5/10/2017.
 */
public class StatementBuilder
{
    private String[] required;
    private ArrayList<Clause> optional;

    public StatementBuilder(String... required)
    {
        this.required = required;
        optional = new ArrayList<>();
    }

    public void setOptional(String value, String condition)
    {
        optional.add(new Clause(value, condition));
    }

    public String makeStatement()
    {
        String sql = "";
        for (String part : required)
        {
            sql += part + " ";
        }
        for (Clause clause : optional)
        {
            String value = clause.getValue();
            if (value != null && !clause.invalid())
            {
                sql += value + " ";
            }
        }
        return StatementUtils.trim(sql).replaceAll(",", ", ");
    }
}
