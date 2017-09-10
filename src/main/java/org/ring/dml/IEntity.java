package org.ring.dml;

import org.ring.dml.transaction.DmlType;

/**
 * Created by quanle on 6/25/2017.
 */
public interface IEntity
{
    default Object insert()
    {
        return Dml.save(this, DmlType.Insert);
    }

    default boolean update()
    {
        return Dml.save(this, DmlType.Update) != null;
    }

    default boolean delete()
    {
        return Dml.delete(this);
    }
}
