package org.ring.dml.statement.later;

import org.ring.dml.statement.interceptor.GetterInterceptor;
import org.ring.oql.expression.Expressible;

/**
 * Created by quanle on 6/30/2017.
 */
public class Clause
{
    private Expressible expression;
    private GetterInterceptor interceptor;

    public Expressible getExpression()
    {
        return expression;
    }

    public GetterInterceptor getInterceptor()
    {
        return interceptor;
    }

    public Clause(Expressible expression, GetterInterceptor interceptor)
    {

        this.expression = expression;
        this.interceptor = interceptor;
    }
}
