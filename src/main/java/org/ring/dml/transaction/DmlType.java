package org.ring.dml.transaction;

import org.ring.meta.annotation.entity.Cascade;

/**
 * Created by quanle on 6/3/2017.
 */
public enum DmlType
{
    Insert,
    Update,
    Delete;

    public static boolean checkCascade(Cascade cascade, DmlType type)
    {
        if (cascade == null)
        {
            return false;
        }

        switch (type)
        {
            case Insert:
                return cascade.insert();

            case Update:
                return cascade.update();

            case Delete:
                return cascade.delete();
        }
        return false;
    }
}
