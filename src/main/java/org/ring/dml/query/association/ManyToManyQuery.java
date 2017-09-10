package org.ring.dml.query.association;

import org.ring.dml.query.EntityFactory;
import org.ring.meta.annotation.relationship.ManyToMany;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

/**
 * Created by quanle on 5/19/2017.
 */
class ManyToManyQuery extends AssociationQuery
{
    ManyToMany association;

    ManyToManyQuery(Field field, Object self, Class<?> entity)
    {
        super(field, self, entity);
        association = field.getAnnotation(ManyToMany.class);
    }

    @Override
    Class<?> getEntity()
    {
        return association.entity();
    }

    @Override
    void setData(ResultSet rs) throws SQLException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException
    {
        Object obj;
        HashSet list = new HashSet();
        while (rs.next())
        {
            obj = EntityFactory.createEntity(memberMapper, rs);
            list.add(obj);
        }
        memberField.set(self, list);
    }

    @Override
    String getQuery()
    {
        return String.format("select target.* from %s target, %s jointable where target.%s = jointable.%s and jointable.%s = ?",
                memberMapper.getTable(), association.joinTable(),
                memberMapper.getPrimaryKey(), association.foreignKey(), association.referencedColumn());
    }
}
