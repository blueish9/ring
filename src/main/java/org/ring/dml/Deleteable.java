package org.ring.dml;

/**
 * Created by quanle on 6/24/2017.
 */
public interface Deleteable
{
    default boolean delete()
    {
        return Dml.delete(this);
    }
}
