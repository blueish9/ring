package org.ring.oql.expression;

/**
 * Created by quanle on 4/29/2017.
 */
class AndExpression extends MultiExpression
{
    AndExpression(Expressible... comparisons)
    {
        super(comparisons);
    }

    public String getOperator()
    {
        return "and";
    }
}
