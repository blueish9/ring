package org.ring.dml;

import org.ring.dml.transaction.DmlType;

/**
 * Created by quanle on 6/24/2017.
 */
public interface Insertable
{
    default Object insert()
    {
       return Dml.save(this, DmlType.Insert);
    }
}
