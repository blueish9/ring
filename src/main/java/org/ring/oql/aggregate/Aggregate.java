package org.ring.oql.aggregate;

import org.ring.oql.parser.Parser;

/**
 * Created by quanle on 6/12/2017.
 */
public class Aggregate
{
    private String function;
    private String field;
    private String resultAlias = null;

    private Aggregate(String function, String field)
    {
        this.function = function;
        this.field = field;
    }

    private Aggregate(String function, String field, String resultAlias)
    {
        this.function = function;
        this.field = field;
        this.resultAlias = resultAlias;
    }

    public String toString(Parser parser) throws NoSuchFieldException
    {
        if (resultAlias == null)
        {
            return String.format("%s(%s)", function, parser.parseField(field));
        }
        return String.format("%s(%s) as %s", function, parser.parseField(field), resultAlias);
    }

    public String getAlias()
    {
        return resultAlias;
    }

    public static Aggregate count(String field)
    {
        return new Aggregate("count", field);
    }

    public static Aggregate count(String field, String resultAlias)
    {
        return new Aggregate("count", field, resultAlias);
    }

    public static Aggregate avg(String field)
    {
        return new Aggregate("avg", field);
    }

    public static Aggregate avg(String field, String resultAlias)
    {
        return new Aggregate("avg", field, resultAlias);
    }

    public static Aggregate sum(String field)
    {
        return new Aggregate("sum", field);
    }

    public static Aggregate sum(String field, String resultAlias)
    {
        return new Aggregate("sum", field, resultAlias);
    }

    public static Aggregate max(String field)
    {
        return new Aggregate("max", field);
    }

    public static Aggregate max(String field, String resultAlias)
    {
        return new Aggregate("max", field, resultAlias);
    }

    public static Aggregate min(String field)
    {
        return new Aggregate("min", field);
    }

    public static Aggregate min(String field, String resultAlias)
    {
        return new Aggregate("min", field, resultAlias);
    }
}
