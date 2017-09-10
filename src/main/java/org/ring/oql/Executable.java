package org.ring.oql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by quanle on 6/11/2017.
 */
public interface Executable
{
    PreparedStatement getJdbcStatement(Connection connection) throws SQLException;
}
