package org.ring.dml.transaction.save;

import org.ring.dml.transaction.joint.JointBatch;
import org.ring.meta.annotation.relationship.ManyToMany;

/**
 * Created by quanle on 6/10/2017.
 */
public class InsertJoint extends JointBatch
{
    public InsertJoint(Class<?> ownerType, ManyToMany association)
    {
        super(ownerType, association);
    }

    @Override
    public String getDml()
    {
        return "insert into %s(%s, %s) values (?, ?)";
    }
}
