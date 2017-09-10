package org.ring.oql.aggregate;

import org.ring.oql.criteria.Comparator;
import org.ring.oql.criteria.Comparison;
import org.ring.oql.parser.Parser;

/**
 * Created by quanle on 6/16/2017.
 */
class AggregateComparison extends Comparison<Aggregate>
{
    AggregateComparison(Aggregate field, Comparator comparator)
    {
        super(field, comparator);
    }

    @Override
    public String print(Parser parser) throws NoSuchFieldException
    {
        parser.addValue(comparator.getValue());
        return field.toString(parser) + comparator.getString();
    }
}
