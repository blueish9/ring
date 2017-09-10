package org.ring.oql.statement;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by quanle on 6/27/2017.
 */
public interface IExecute<T>
{
    void prepare(PreparedStatement statement);

    T retrieve(ResultSet resultSet) throws SQLException;
}
