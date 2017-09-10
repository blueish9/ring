package org.ring.oql.statement;

import org.ring.oql.INonQuery;
import org.ring.oql.builder.StatementBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by quanle on 4/30/2017.
 */
public class DeleteStatement extends Statement implements INonQuery
{
    String delete = "delete";

    public DeleteStatement(Class<?> entity)
    {
        super(entity);
    }

    @Override
    public String generateStatement()
    {
        StatementBuilder builder = getBuilder(delete, from);
        return builder.makeStatement();
    }

    @Override
    public PreparedStatement getJdbcStatement(Connection connection) throws SQLException
    {
        return getJdbcStatement(connection, parser.getValues());
    }

    @Override
    public int execute() throws SQLException
    {
        return execute(parser.getValues());
    }
}
