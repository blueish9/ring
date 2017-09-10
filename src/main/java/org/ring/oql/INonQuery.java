package org.ring.oql;

import org.ring.orm.OrmFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by quanle on 6/28/2017.
 */
public interface INonQuery extends Executable
{
    int execute() throws SQLException;

    default int execute(ArrayList... values) throws SQLException
    {
        Connection connection = OrmFactory.getConnection();
        PreparedStatement statement = getJdbcStatement(connection);

        int i = 1;
        for (ArrayList list : values)
        {
            for (Object val : list)
            {
                statement.setObject(i, val);
                i++;
            }
        }

        int rows = statement.executeUpdate();
        statement.close();
        connection.close();
        return rows;
    }
}
