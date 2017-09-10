package org.ring.oql.aggregate;

import org.ring.oql.criteria.Comparator;
import org.ring.oql.expression.Expressible;

/**
 * Created by quanle on 4/28/2017.
 */
public final class AggregateCriteria
{
    public static Expressible equal(Aggregate aggregate, Object value)
    {
        return new AggregateComparison(aggregate, new Comparator(value, "="));
    }

    public static Expressible gt(Aggregate aggregate, Object value)
    {
        return new AggregateComparison(aggregate, new Comparator(value, ">"));
    }

    public static Expressible lt(Aggregate aggregate, Object value)
    {
        return new AggregateComparison(aggregate, new Comparator(value, "<"));
    }
}
