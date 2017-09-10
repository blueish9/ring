package org.ring.oql.criteria;


import org.ring.oql.expression.Expressible;

/**
 * Created by quanle on 6/13/2017.
 */
public abstract class Comparison<T> implements Expressible
{
    protected T field;
    protected Comparator comparator;

    public Comparison(T field, Comparator comparator)
    {
        this.field = field;
        this.comparator = comparator;
    }
}
