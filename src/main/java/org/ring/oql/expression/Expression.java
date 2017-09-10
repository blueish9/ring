package org.ring.oql.expression;

/**
 * Created by quanle on 6/13/2017.
 */
public final class Expression
{
    public static Expressible or(Expressible... expressions)
    {
        return new OrExpression(expressions);
    }

    public static Expressible and(Expressible... expressions)
    {
        return new AndExpression(expressions);
    }
}
