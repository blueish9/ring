package org.ring.oql.criteria;

import org.ring.oql.parser.JoinParser;
import org.ring.oql.parser.Parser;

/**
 * Created by quanle on 4/29/2017.
 */
public class FieldComparison extends Comparison<String>
{
    FieldComparison(String field, Comparator comparator)
    {
        super(field, comparator);
    }

    public String print(Parser parser) throws NoSuchFieldException
    {
        if (comparator instanceof Join)
        {
            ((JoinParser) parser).add(field, comparator.getValue().toString());
            return parser.parseField(field, comparator.getValue().toString());
        }

        parser.addValue(comparator.getValue());
        return parser.parseField(field) + comparator.getString();
    }
}
