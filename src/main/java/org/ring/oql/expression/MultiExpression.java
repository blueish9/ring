package org.ring.oql.expression;

import org.ring.oql.parser.Parser;

/**
 * Created by quanle on 4/29/2017.
 */
abstract class MultiExpression implements Expressible
{
    private Expressible[] expressions;

   MultiExpression(Expressible... expressions)
    {
        this.expressions = expressions;
    }

    abstract String getOperator();

    public String print(Parser parser) throws NoSuchFieldException
    {
        String condition = "";
        for (Expressible e : expressions)
        {
            condition += e.print(parser) + " " + getOperator() + " ";
        }
        return "(" + condition.substring(0, condition.length() - getOperator().length() - 2) + ")";
    }
}
