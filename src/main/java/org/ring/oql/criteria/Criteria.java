package org.ring.oql.criteria;

import org.ring.oql.expression.Expressible;

/**
 * Created by quanle on 4/28/2017.
 */
public final class Criteria
{
    public static Expressible join(String field, String value)
    {
        return new FieldComparison(field, new Join(value));
    }

    public static Expressible equal(String field, Object value)
    {
        return new FieldComparison(field, new Comparator(value, "="));
    }

    public static Expressible gt(String field, Object value)
    {
        return new FieldComparison(field, new Comparator(value, ">"));
    }

    public static Expressible lt(String field, Object value)
    {
        return new FieldComparison(field, new Comparator(value, "<"));
    }





    public static Expressible equal(Object field, Object value)
    {
        return new FieldComparison("", new Comparator(value, "="));
    }

    public static Expressible gt(Object field, Object value)
    {
        return new FieldComparison("", new Comparator(value, ">"));
    }

    public static Expressible lt(Object field, Object value)
    {
        return new FieldComparison("", new Comparator(value, "<"));
    }
}
