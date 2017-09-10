package org.ring.oql;

import org.ring.oql.aggregate.Aggregate;
import org.ring.oql.expression.Expressible;
import org.ring.oql.statement.IExecute;
import org.ring.orm.OrmFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

/**
 * Created by quanle on 5/9/2017.
 */
public interface IQuery extends Executable
{
    void sortDescending(String... fields);

    void sortAscending(String... fields);

    void setProjection(String... fields);

    void setAggregate(Aggregate... aggregates);

    void groupBy(String... fields);

    void setHaving(Expressible expression);

    void setCriteria(Expressible expression);

    String generateStatement();

    List<HashMap<String, Object>> retrieveProjections() throws SQLException;

    default <T> T execute(IExecute<T> execute) throws SQLException
    {
        Connection connection = OrmFactory.getConnection();
        PreparedStatement statement = getJdbcStatement(connection);
        execute.prepare(statement);
        ResultSet rs = statement.executeQuery();
        T result = execute.retrieve(rs);
        rs.close();
        statement.close();
        connection.close();
        return result;
    }

    default  <T> T execute(Function<ResultSet, T> function) throws SQLException
    {
        Connection connection = OrmFactory.getConnection();
        PreparedStatement statement = getJdbcStatement(connection);
        ResultSet rs = statement.executeQuery();
        T result = function.apply(rs);
        rs.close();
        statement.close();
        connection.close();
        return result;
    }
}
