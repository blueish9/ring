package org.ring.dml;

import org.ring.dml.transaction.DmlType;

/**
 * Created by quanle on 6/24/2017.
 */
public interface Updateable
{
    default boolean update()
    {
        return Dml.save(this, DmlType.Update) != null;
    }
}
