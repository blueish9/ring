package org.ring.oql.statement;

import org.ring.entity.EntityManager;
import org.ring.oql.INonQuery;
import org.ring.oql.builder.StatementBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by quanle on 4/30/2017.
 */
public class UpdateStatement extends Statement implements INonQuery
{
    String update = "update ";
    String set = "set ";
    ArrayList values;

    public UpdateStatement(Class<?> entity)
    {
        super(entity);
        values = new ArrayList();
        update += EntityManager.getTable(entity);
    }

    public void setValue(String field, Object value)
    {
        // check aliasMapping == null or != null and throw exception
        values.add(value);
        set += getColumn(field) + " = ?,";
    }

    @Override
    public String generateStatement()
    {
        update = trim(update);
        set = trim(set);
        StatementBuilder builder = getBuilder(update, set);
        return builder.makeStatement();
    }

    @Override
    public PreparedStatement getJdbcStatement(Connection connection) throws SQLException
    {
        return getJdbcStatement(connection, values, parser.getValues());
    }


    @Override
    public int execute() throws SQLException
    {
        return execute(values, parser.getValues());
    }
}
