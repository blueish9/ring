package org.ring.dml.transaction.delete;

import org.ring.dml.transaction.joint.JointBatch;
import org.ring.meta.annotation.relationship.ManyToMany;

/**
 * Created by quanle on 6/10/2017.
 */
public class DeleteJoint extends JointBatch
{
    public DeleteJoint(Class<?> ownerType, ManyToMany association)
    {
        super(ownerType, association);
    }

    @Override
    public String getDml()
    {
        return "delete from %s where %s = ? and %s = ?";
    }
}
