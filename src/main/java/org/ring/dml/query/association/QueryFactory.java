package org.ring.dml.query.association;

import org.ring.meta.annotation.relationship.ManyToMany;
import org.ring.meta.annotation.relationship.ManyToOne;
import org.ring.meta.annotation.relationship.OneToMany;
import org.ring.meta.annotation.relationship.OneToOne;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * Created by quanle on 6/24/2017.
 */
public final class QueryFactory
{
    public static AssociationQuery initQuery(Class<? extends Annotation> type, Field field, Object self, Class<?> entity)
    {
        if (type == OneToOne.class)
        {
            return new OneToOneQuery(field, self, entity);
        }
        if (type == OneToMany.class)
        {
            return new OneToManyQuery(field, self, entity);
        }
        if (type == ManyToOne.class)
        {
            return new ManyToOneQuery(field, self, entity);
        }
        if (type == ManyToMany.class)
        {
            return new ManyToManyQuery(field, self, entity);
        }
        return null;
    }
}
