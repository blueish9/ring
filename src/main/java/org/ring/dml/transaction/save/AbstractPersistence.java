package org.ring.dml.transaction.save;

import org.ring.dml.transaction.Division;
import org.ring.entity.EntityManager;
import org.ring.entity.Mapper;
import org.ring.exception.InvalidDataException;
import org.ring.meta.annotation.relationship.OneToMany;
import org.ring.orm.OrmFactory;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

/**
 * Created by quanle on 6/2/2017.
 */
public abstract class AbstractPersistence extends Division
{
    protected Mapper mapper;

    public void setMapper(Mapper mapper)
    {
        this.mapper = mapper;
    }

    public boolean add(Object data) throws InvalidDataException
    {
        InvalidDataException exception = mapper.validate(data);
        if (exception != null)
        {
            throw exception;
        }

        return dataSet.add(data);
    }

    /**
     * traverse all items in dataSet
     * with each item, traverse all foreign keys, then use setObject() method to set the correspondent data
     * after each item's traverse, set id for the owner's id (in where clause), then add to batch
     */
    public void updateForeignKey(Connection connection)
    {
        try
        {
            PreparedStatement statement = mapper.getUpdateForeignKeyStatement(connection);
            if (statement != null)
            {
                Mapper memberMapper;
                Object foreignKey;
                Field[] fields = mapper.getForeignKeys();
                for (Object item : dataSet)
                {
                    int j = 0;
                    for (int i = 0; i < fields.length; ++i)
                    {
                        foreignKey = fields[i].get(item);
                        if (foreignKey != null)
                        {
                            memberMapper = EntityManager.getMapper(foreignKey.getClass());
                            statement.setObject(i + 1, memberMapper.getId(foreignKey));
                        }
                        else
                        {
                            statement.setObject(i + 1, null);
                            j++;
                        }
                    }

                    if (j < fields.length)
                    {
                        statement.setObject(fields.length + 1, mapper.getId(item));
                        statement.addBatch();
                    }
                    // if j == fields.length, it means that all foreign keys are null --> no need to addBatch
                }
                statement.executeBatch();
                statement.close();
            }
        }
        catch (SQLException | IllegalAccessException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * traverse all @OneToMany fields and update referencedColumn with owner's id
     */
    public void updateReferencedColumn(Connection connection)
    {
        try
        {
            String ifNullFunction = OrmFactory.getIfNullFunction();
            for (Field field : mapper.getMembers(OneToMany.class))
            {
                OneToMany association = field.getAnnotation(OneToMany.class);
                Mapper memberMapper = EntityManager.getMapper(association.entity());
                String table = memberMapper.getTable();
                String referencedColumn = association.referencedColumn();
                PreparedStatement statement = connection.prepareStatement(
                        String.format("update %s set %s = %s(?, %s) where %s = ?",
                                table, referencedColumn, ifNullFunction, referencedColumn, memberMapper.getPrimaryKey()));

                for (Object item : dataSet)
                {
                    Object rcData = field.get(item);
                    if (rcData instanceof Collection)   // check cascade ??
                    {
                        Collection collection = (Collection) rcData;
                        if (collection.size() > 0)
                        {
                            statement.setObject(1, mapper.getId(item));
                            for (Object i : collection)
                            {
                                statement.setObject(2, memberMapper.getId(i));
                                statement.addBatch();
                            }
                        }
                    }
                }

                statement.executeBatch();
                statement.close();
            }
        }
        catch (SQLException | IllegalAccessException e)
        {
            e.printStackTrace();
        }
    }

    // consider checking exist in 1 batch ???
    protected boolean isExist(PreparedStatement statement, Object id)
    {
        try
        {
            statement.setObject(1, id);
            ResultSet rs = statement.executeQuery();
            return rs.isBeforeFirst();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    // this method will make changes to statement
    protected void setInsertParameter(PreparedStatement statement, Object data) throws SQLException, IllegalAccessException
    {
        setColumnParameter(statement, data);
    }

    // this method will make changes to statement
    protected void setUpdateParameter(PreparedStatement statement, Object data, Object id) throws SQLException, IllegalAccessException
    {
        setColumnParameter(statement, data);
        statement.setObject(mapper.getColumns().length, id);
        statement.addBatch();
    }

    // this method will make changes to statement
    private void setColumnParameter(PreparedStatement statement, Object data) throws IllegalAccessException, SQLException
    {
        Field[] fields = mapper.getColumns();
        if (mapper.isIdAutoGenerated())
        {
            int i = 1;
            Field idField = mapper.getIdField();
            for (Field field : fields)
            {
                if (idField != field)
                {
                    statement.setObject(i, field.get(data));
                    i++;
                }
            }
        }
        else
        {
            for (int i = 0; i < fields.length; ++i)
            {
                statement.setObject(i + 1, fields[i].get(data));
            }
        }
    }
}

