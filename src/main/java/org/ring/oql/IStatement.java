package org.ring.oql;

import org.ring.oql.expression.Expressible;

import java.util.Set;

/**
 * Created by quanle on 5/11/2017.
 */
public interface IStatement
{
    String getColumn(String field);

    Set<String> allColumns();



    void setCriteria(Expressible expression);

    String generateStatement();
}
