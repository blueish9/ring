package org.ring.dml.query.association;

import org.ring.dml.query.EntityFactory;
import org.ring.meta.annotation.relationship.ManyToOne;
import org.ring.meta.annotation.relationship.OneToMany;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

/**
 * Created by quanle on 5/19/2017.
 */
class OneToManyQuery extends AssociationQuery
{
    private OneToMany association;

    OneToManyQuery(Field field, Object self, Class<?> entity)
    {
        super(field, self, entity);
        association = field.getAnnotation(OneToMany.class);
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
        Field mirror = getMirror();
        if (mirror != null)
        {
            while (rs.next())
            {
                obj = EntityFactory.createEntity(memberMapper, rs);
                list.add(obj);

                mirror.set(obj, self);
            }
        }
        else
        {
            while (rs.next())
            {
                obj = EntityFactory.createEntity(memberMapper, rs);
                list.add(obj);
            }
        }

        memberField.set(self, list);
    }

    @Override
    String getQuery()
    {
        return String.format("select * from %s where %s = ?",
                memberMapper.getTable(), association.referencedColumn());
    }

    private Field getMirror()
    {
        for (Field field : memberMapper.getMembers(OneToMany.class))
        {
            if (field.getType() == holderMapper.getEntity())
            {
                ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);
                if (manyToOne != null && manyToOne.foreignKey().equals(association.referencedColumn()))
                {
                    field.setAccessible(true);
                   return field;
                }
            }
        }
        return null;
    }
}
