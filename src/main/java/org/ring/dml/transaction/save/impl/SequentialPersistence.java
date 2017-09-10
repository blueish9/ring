package org.ring.dml.transaction.save.impl;

import org.ring.dml.transaction.save.converter.IdConverter;
import org.ring.dml.transaction.save.AbstractPersistence;
import org.ring.orm.OrmFactory;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by quanle on 6/8/2017.
 */
public class SequentialPersistence extends AbstractPersistence
{
    @Override
    public void execute(Connection connection)
    {
        try
        {
            // prepare
            PreparedStatement updateStatement = mapper.getUpdateStatement(connection);
            PreparedStatement existStatement = mapper.getExistStatement(connection);
            PreparedStatement insertStatement = mapper.getInsertStatement(connection);

            // traverseAll, setInsertParameter, setInsertParameter
            Field idField = mapper.getIdField();
            for (Object item : dataSet)
            {
                Object id = mapper.getId(item);
                if (id != null)
                {
                    if (isExist(existStatement, id))
                    {
                        setUpdateParameter(updateStatement, item, id);
                    }
                    else
                    {
                        save(insertStatement, item, idField);
                    }
                }
                else        // id == null and autoGenerated == false (due to pre-check)
                {
                    save(insertStatement, item, idField);
                }
            }

            updateStatement.executeBatch();

            insertStatement.close();
            updateStatement.close();
        }
        catch (SQLException | IllegalAccessException e)
        {
            e.printStackTrace();
        }
    }

    void save(PreparedStatement statement, Object data, Field idField) throws SQLException, IllegalAccessException
    {
        setInsertParameter(statement, data);
        int rows = statement.executeUpdate();
        if (rows > 0)
        {
            ResultSet resultSet = statement.getGeneratedKeys();
            if (resultSet.next())
            {
                Object id = resultSet.getObject(1);
                IdConverter converter = OrmFactory.getIdConverter();
                converter.setId(data, idField, id);
            }
        }
    }
}
