package org.ring.dml.transaction.delete;

import org.ring.dml.transaction.DmlTransaction;
import org.ring.dml.transaction.DmlType;
import org.ring.dml.transaction.joint.Joint;
import org.ring.entity.EntityManager;
import org.ring.entity.Mapper;
import org.ring.exception.InvalidDataException;
import org.ring.meta.annotation.entity.Cascade;
import org.ring.meta.annotation.relationship.ManyToMany;
import org.ring.meta.annotation.relationship.OneToMany;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by quanle on 6/4/2017.
 */
public class DeleteTransaction extends DmlTransaction
{
    private HashMap<Class<?>, BatchDelete> dmlMap = new HashMap<>();
    private HashMap<String, DeleteJoint> jointMap = new HashMap<>();
    private HashSet<Object> rcSet = new HashSet<>();

    public DeleteTransaction()
    {
        dmlType = DmlType.Delete;
    }

    @Override
    public Boolean execute(Connection connection, Object data)
    {
        try
        {
            parse(data, data.getClass(), false);

            dmlMap.forEach((type, batch) ->
            {
                batch.removeForeignKey(connection);
            });

            jointMap.forEach((type, batch) ->
            {
                batch.execute(connection);
            });

            dmlMap.forEach((type, batch) ->
            {
                batch.execute(connection);
            });

            return true;
        }
        catch (IllegalAccessException | SQLException | InvalidDataException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    private void parse(Object data, Class<?> type, boolean isRc) throws SQLException, IllegalAccessException, InvalidDataException
    {
        if (data == null)
        {
            return;
        }

        boolean notExist;
        type = EntityManager.original(type);
        Mapper mapper = EntityManager.getMapper(type);

        if (isRc)
        {
            notExist = rcSet.add(data);
        }
        else
        {
            BatchDelete dml = dmlMap.get(type);
            if (dml == null)
            {
                dml = new BatchDelete(mapper);
                dmlMap.put(type, dml);
            }
            notExist = dml.add(data);
        }

        if (notExist)
        {
            for (Field field : mapper.getForeignKeys())
            {
                if (DmlType.checkCascade(field.getAnnotation(Cascade.class), dmlType))
                {
                    parse(field.get(data), field.getType(), false);
                }
            }

            parseReferencedColumns(data, mapper.getMembers(OneToMany.class), true,
                    field -> field.getAnnotation(OneToMany.class).entity(),
                    (item, field) ->
                    {
                    }
            );

            parseReferencedColumns(data, mapper.getMembers(ManyToMany.class), false,
                    field -> field.getAnnotation(ManyToMany.class).entity(),
                    (item, field) ->
                    {
                        Mapper memberMapper = EntityManager.getMapper(item.getClass());
                        Object memberId = memberMapper.getId(item);
                        Object id = mapper.getId(data);
                        if (id != null && memberId != null)
                        {
                            String table = memberMapper.getTable();
                            DeleteJoint joint = jointMap.get(table);
                            if (joint == null)
                            {
                                joint = new DeleteJoint(data.getClass(), field.getAnnotation(ManyToMany.class));
                                jointMap.put(table, joint);
                            }
                            joint.add(new Joint(data, item));
                        }
                    }
            );
        }
    }

    private void parseReferencedColumns(Object data, Field[] referencedColumns, boolean isRc, GetAssociation getAssociation, AddJoinTable addJoinTable) throws IllegalAccessException, SQLException, InvalidDataException
    {
        for (Field field : referencedColumns)
        {
            Object obj = field.get(data);
            if (obj instanceof Collection && DmlType.checkCascade(field.getAnnotation(Cascade.class), dmlType))
            {
                Collection collection = (Collection) obj;
                Class<?> type = getAssociation.invoke(field);
                for (Object item : collection)
                {
                    addJoinTable.invoke(item, field);
                    parse(item, type, isRc);
                }
            }
        }
    }

    private interface AddJoinTable
    {
        void invoke(Object data, Field field) throws IllegalAccessException;
    }

    private interface GetAssociation
    {
        Class<?> invoke(Field field);
    }
    /*@Override
    public Boolean execute(Connection connection, Object data)
    {
        try
        {
            parse(data, data.getClass());

            dmlMap.forEach((type, batch) -> batch.removeForeignKey(connection));

            jointMap.forEach((type, batch) -> batch.execute(connection));

            dmlMap.forEach((type, batch) -> batch.execute(connection));

            return true;
        }
        catch (IllegalAccessException | SQLException | InvalidDataException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void parseOneToMany(Object data, Mapper mapper)
    {

    }

    @Override
    protected BatchDelete getDml(Mapper mapper)
    {
        return new BatchDelete(mapper);
    }

    @Override
    protected DeleteJoint getJoint(Class<?> ownerType, ManyToMany association)
    {
        return new DeleteJoint(ownerType, association);
    }

    @Override
    protected boolean checkJointId(Object fkId, Object rcId)
    {
        return fkId != null && rcId != null;
    }*/
}
