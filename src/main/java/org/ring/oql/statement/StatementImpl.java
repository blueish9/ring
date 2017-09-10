package org.ring.oql.statement;

import org.ring.oql.builder.StatementBuilder;
import org.ring.oql.IStatement;
import org.ring.oql.expression.Expressible;
import org.ring.oql.parser.Parser;
import org.ring.oql.StatementUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by quanle on 5/9/2017.
 */
abstract class StatementImpl implements IStatement
{
    String from = "from ";
    String where = null;
    Parser parser;

    public void setCriteria(Expressible expression)
    {
        try
        {
            String criteria = expression.print(parser);
            if (criteria.startsWith("(") && criteria.endsWith(")"))
            {
                criteria = criteria.substring(1, criteria.length() - 1);
            }
            where = "where " + criteria;

            String joinTable = parser.getJoinTable();
            if (joinTable != null)
            {
                from += joinTable;
            }
        }
        catch (NoSuchFieldException e)
        {
            e.printStackTrace();
        }
    }

    String trim(String statement)
    {
        return StatementUtils.trim(statement);
    }

    StatementBuilder getBuilder(String... required)
    {
        StatementBuilder builder = new StatementBuilder(required);
        builder.setOptional(where, null);
        return builder;
    }

    protected PreparedStatement getJdbcStatement(Connection connection, ArrayList... values) throws SQLException
    {
        String sql = generateStatement();
        System.out.println(sql);
        PreparedStatement statement = connection.prepareStatement(sql);
        int i = 1;
        for (ArrayList list : values)
        {
            for (Object value : list)
            {
                statement.setObject(i, value);
                i++;
            }
        }
        return statement;       // must close statement ??????????????????????
    }
}
