package org.ring.dml.query.association;

import org.ring.dml.query.EntityFactory;
import org.ring.meta.annotation.relationship.ManyToOne;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by quanle on 5/19/2017.
 */
class ManyToOneQuery extends AssociationQuery
{
    private ManyToOne association;

    ManyToOneQuery(Field field, Object self, Class<?> entity)
    {
        super(field, self, entity);
        association = field.getAnnotation(ManyToOne.class);
    }

    @Override
    Class<?> getEntity()
    {
        return association.entity();
    }

    @Override
    void setData(ResultSet rs) throws SQLException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException
    {
        rs.next();
        Object obj = EntityFactory.createEntity(memberMapper, rs);
        memberField.set(self, obj);
    }

    @Override
    String getQuery()
    {
        return String.format("select member.* from %s holder, %s member where holder.%s = member.%s and holder.%s = ?",
                holderMapper.getTable(),  memberMapper.getTable(),
                association.foreignKey(), memberMapper.getPrimaryKey(), holderMapper.getPrimaryKey());
    }
}
