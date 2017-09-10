package org.ring.oql.expression;

import org.ring.oql.parser.Parser;

/**
 * Created by quanle on 6/13/2017.
 */
public interface Expressible
{
    String print(Parser parser) throws NoSuchFieldException;
}
