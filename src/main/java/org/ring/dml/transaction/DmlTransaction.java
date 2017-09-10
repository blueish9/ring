package org.ring.dml.transaction;

import java.sql.Connection;

/**
 * Created by quanle on 6/4/2017.
 */
public abstract class DmlTransaction
{
    protected DmlType dmlType;

    public abstract Object execute(Connection connection, Object data);
}
