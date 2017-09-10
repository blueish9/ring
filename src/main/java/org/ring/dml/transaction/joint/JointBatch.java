package org.ring.dml.transaction.joint;

import org.ring.dml.transaction.Division;
import org.ring.entity.EntityManager;
import org.ring.entity.Mapper;
import org.ring.meta.annotation.relationship.ManyToMany;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by quanle on 6/3/2017.
 */
public abstract class JointBatch extends Division<Joint>
{
    Class<?> ownerType;
    ManyToMany association;

    public JointBatch(Class<?> ownerType, ManyToMany association)
    {
        super();
        this.ownerType = ownerType;
        this.association = association;
    }

    @Override
    public boolean add(Joint joint)
    {
        // all Joint in dataSet will have objFk with the same type as ownerType
        if (joint.getObjFk().getClass() == ownerType)
        {
            return dataSet.add(joint);
        }
        return dataSet.add(new Joint(joint.getObjRc(), joint.getObjFk()));
    }

    @Override
    public void execute(Connection connection)
    {
        try
        {
            if (dataSet.size() > 0)
            {
                Mapper mapperOwner = EntityManager.getMapper(ownerType);
                Mapper mapperMember = EntityManager.getMapper(association.entity());

                String dml = String.format(getDml(),
                        association.joinTable(), association.foreignKey(), association.referencedColumn());
                PreparedStatement statement = connection.prepareStatement(dml);

                for (Joint item : dataSet)
                {
                    statement.setObject(1, mapperMember.getId(item.getObjRc()));
                    statement.setObject(2, mapperOwner.getId(item.getObjFk()));
                    statement.addBatch();
                }
                statement.executeBatch();
                statement.close();
            }
        }
        catch (SQLException | IllegalAccessException e)
        {
            e.printStackTrace();
        }
    }

    public abstract String getDml();
}
