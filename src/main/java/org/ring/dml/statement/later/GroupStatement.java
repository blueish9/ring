package org.ring.dml.statement.later;

import org.ring.dml.statement.interceptor.GetterInterceptor;
import org.ring.entity.Mapper;
import org.ring.oql.expression.Expressible;

/**
 * Created by quanle on 7/1/2017.
 */
public class GroupStatement
{
    private Mapper mapper;
    private Expressible whereExpression;
    private GetterInterceptor whereInterceptor;
    private GetterInterceptor groupInterceptor;
}
