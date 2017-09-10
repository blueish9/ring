package org.ring.dml.query.association;

import org.ring.entity.EntityManager;
import org.ring.entity.Mapper;
import org.ring.orm.OrmFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by quanle on 5/19/2017.
 */
public abstract class AssociationQuery
{
    private Object id;
    Mapper holderMapper;
    Mapper memberMapper;
    Object self;
    Field memberField;

    AssociationQuery(Field memberField, Object self, Class<?> entity)
    {
        this.memberField = memberField;
        this.self = self;

        holderMapper = EntityManager.getMapper(entity);
        try
        {
            id = holderMapper.getId(self);
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
    }

    public boolean execute() throws SQLException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException
    {
        boolean success = false;
        memberMapper = EntityManager.getMapper(getEntity());
        if (memberMapper != null)
        {
            String sql = getQuery();
            Connection connection = OrmFactory.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setObject(1, id);
            ResultSet rs = statement.executeQuery();
            if (rs.isBeforeFirst())
            {
                setData(rs);
                success = true;
            }
            rs.close();
            statement.close();
            connection.close();
        }
        return success;
    }

    abstract Class<?> getEntity();

    abstract void setData(ResultSet rs) throws SQLException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException;

    abstract String getQuery();
}
