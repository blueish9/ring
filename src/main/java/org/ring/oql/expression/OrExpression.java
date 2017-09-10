package org.ring.oql.expression;

/**
 * Created by quanle on 4/29/2017.
 */
class OrExpression extends MultiExpression
{
    OrExpression(Expressible... comparisons)
    {
        super(comparisons);
    }

    public String getOperator()
    {
        return "or";
    }
}
