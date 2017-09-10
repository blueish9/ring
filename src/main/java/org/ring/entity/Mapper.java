package org.ring.entity;

import org.ring.exception.EntityMappingException;
import org.ring.exception.InvalidDataException;
import org.ring.meta.annotation.entity.Column;
import org.ring.meta.annotation.entity.Id;
import org.ring.meta.annotation.entity.Table;
import org.ring.meta.annotation.relationship.ManyToMany;
import org.ring.meta.annotation.relationship.ManyToOne;
import org.ring.meta.annotation.relationship.OneToMany;
import org.ring.meta.annotation.relationship.OneToOne;
import org.jetbrains.annotations.NotNull;
import org.ring.orm.OrmFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by quanle on 5/14/2017.
 */
public class Mapper
{
    private Class<?> entity;
    private String table;
    private HashMap<String, String> columnMapping;

    private int idPos = -1;
    private Field[] columns;
    private String[] foreignColumns;

    private HashMap<Field, Class<? extends Annotation>> associationMapping;
    private HashMap<Class<? extends Annotation>, Field[]> fieldMapping;
    private boolean isLeaf = false;

    private String insert;
    private String update;
    private String delete;
    private String exist;
    private String updateForeignKey;
    private String updateNull;

    Mapper(Class<?> entity) throws EntityMappingException
    {
        this.entity = entity;
        Table tableAnnotation = entity.getAnnotation(Table.class);
        if (tableAnnotation == null)
        {
            throw new EntityMappingException("Table not found");
        }

        table = tableAnnotation.value();
        associationMapping = new HashMap<>();
        columnMapping = new HashMap<>();

        HashMap<Class<? extends Annotation>, ArrayList<Field>> map = new HashMap<>();
        map.put(OneToOne.class, new ArrayList<>());
        map.put(ManyToOne.class, new ArrayList<>());
        map.put(OneToMany.class, new ArrayList<>());
        map.put(ManyToMany.class, new ArrayList<>());

        ArrayList<String> foreign = new ArrayList<>();
        ArrayList<Field> fields = new ArrayList<>();
        for (Field field : entity.getDeclaredFields())
        {
            field.setAccessible(true);

            Column annotation = field.getAnnotation(Column.class);
            if (annotation != null)
            {
                String fieldName = field.getName();
                String columnName = annotation.name();
                columnMapping.put(fieldName, columnName);

                if (field.isAnnotationPresent(Id.class))
                {
                    idPos = fields.size();
                }
                fields.add(field);
            }
            else
            {
                OneToOne oneToOne = field.getAnnotation(OneToOne.class);
                if (oneToOne != null)
                {
                    foreign.add(oneToOne.foreignKey());
                    addAssociation(map, field, OneToOne.class);
                    continue;
                }

                ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);
                if (manyToOne != null)
                {
                    foreign.add(manyToOne.foreignKey());
                    addAssociation(map, field, ManyToOne.class);
                    continue;
                }

                if (field.isAnnotationPresent(OneToMany.class))
                {
                    addAssociation(map, field, OneToMany.class);
                    continue;
                }
                if (field.isAnnotationPresent(ManyToMany.class))
                {
                    addAssociation(map, field, ManyToMany.class);
                    continue;
                }
            }
        }

        if (idPos == -1)
        {
            throw new EntityMappingException("Id not found");
        }
        else
        {
            this.columns = fields.toArray(new Field[fields.size()]);
            foreignColumns = foreign.toArray(new String[foreign.size()]);

            fieldMapping = new HashMap<>();
            map.forEach((key, value) -> fieldMapping.put(key, value.toArray(new Field[value.size()])));

            isLeaf = fieldMapping.get(OneToOne.class).length == 0 && fieldMapping.get(ManyToOne.class).length == 0 &&
                    fieldMapping.get(OneToMany.class).length == 0 && fieldMapping.get(ManyToMany.class).length == 0;

            Field field = getIdField();
            if (field.getType() != Integer.class && field.getType() != Long.class)
            {
                throw new EntityMappingException("Id must be declared either Integer or Long");
            }
        }

        String id = columnMapping.get(getIdField().getName());
        constructInsert();
        constructUpdate(id);
        constructDelete(id);
        constructUpdateAssociation(id);
        constructUpdateNull(id);
        constructExist(id);
    }

    private void addAssociation(HashMap<Class<? extends Annotation>, ArrayList<Field>> map, Field field, Class<? extends Annotation> association)
    {
        associationMapping.put(field, association);
        map.get(association).add(field);
    }

    private void constructUpdateAssociation(String id)
    {
        if (getForeignKeys().length == 0)
        {
            updateForeignKey = null;
        }
        else
        {
            String columnName;
            String ifNullFunction = OrmFactory.getIfNullFunction();
            updateForeignKey = "update " + getTable() + " set ";
            for (Field field : fieldMapping.get(OneToOne.class))
            {
                columnName = field.getAnnotation(OneToOne.class).foreignKey();
                updateForeignKey += String.format("%s = %s(?, %s),", columnName, ifNullFunction, columnName);
            }
            for (Field field : fieldMapping.get(ManyToOne.class))
            {
                columnName = field.getAnnotation(ManyToOne.class).foreignKey();
                updateForeignKey += String.format("%s = %s(?, %s),", columnName, ifNullFunction, columnName);
            }
            updateForeignKey = trim(updateForeignKey) + " where " + id + "=?";
        }
    }

    private void constructUpdateNull(String id)
    {
        if (getForeignKeys().length == 0)
        {
            updateNull = null;
        }
        else
        {
            String columnName;
            updateNull = "update " + getTable() + " set ";
            for (Field field : fieldMapping.get(OneToOne.class))
            {
                columnName = field.getAnnotation(OneToOne.class).foreignKey();
                updateNull += String.format("%s = null,", columnName);
            }
            for (Field field : fieldMapping.get(ManyToOne.class))
            {
                columnName = field.getAnnotation(ManyToOne.class).foreignKey();
                updateNull += String.format("%s = null,", columnName);
            }
            updateNull = trim(updateNull) + " where " + id + "=?";
        }
    }

    private void constructDelete(String id)
    {
        delete = "delete from " + getTable() + " where " + id + "=?";
    }

    private void constructExist(String id)
    {
        exist = "select 1 from " + getTable() + " where " + id + " =?";
    }

    private void constructUpdate(String id)
    {
        update = "update " + getTable() + " set ";
        if (isIdAutoGenerated())
        {
            for (Field field : this.columns)
            {
                if (field != getIdField())
                {
                    String columnName = columnMapping.get(field.getName());
                    update += columnName + "=?,";
                }
            }
        }
        else
        {
            for (Field field : this.columns)
            {
                String columnName = columnMapping.get(field.getName());
                update += columnName + "=?,";
            }
        }
        update = trim(update) + " where " + id + "=?";
    }

    private void constructInsert()
    {
        String columns = "";
        String values = "";
        if (isIdAutoGenerated())
        {
            for (Field field : this.columns)
            {
                if (field != getIdField())
                {
                    columns += getColumn(field.getName()) + ",";
                    values += "?,";
                }
            }
        }
        else
        {
            for (Field field : this.columns)
            {
                columns += getColumn(field.getName()) + ",";
                values += "?,";
            }
        }
        insert = String.format("insert into %s(%s) values (%s)", getTable(), trim(columns), trim(values));
    }

    private String trim(String s)
    {
        return s.substring(0, s.length() - 1);
    }

    public Class<?> getEntity()
    {
        return entity;
    }

    @NotNull
    public String getTable()
    {
        return table;
    }

    public String getColumn(String field)
    {
        return columnMapping.get(field);
    }

    @NotNull
    public Field getIdField()
    {
        return columns[idPos];
    }

    public Object getId(Object data) throws IllegalAccessException
    {
        return columns[idPos].get(data);
    }

    public String getPrimaryKey()
    {
        return columns[idPos].getAnnotation(Column.class).name();
    }

    public boolean isIdAutoGenerated()
    {
        return getIdField().getAnnotation(Id.class).autoGenerate();
    }

    @NotNull
    public Field[] getColumns()
    {
        return columns;
    }

    public Set<String> getTableColumns()
    {
      //  ArrayList<String> columns = new ArrayList<>(columnMapping.keySet());
  //      Collections.addAll(columns, foreignColumns);
        return new HashSet<>(columnMapping.values());
    }

    @NotNull
    public Field[] getForeignKeys()
    {
        ArrayList<Field> list = new ArrayList<>();
        Collections.addAll(list, fieldMapping.get(OneToOne.class));
        Collections.addAll(list, fieldMapping.get(ManyToOne.class));
        return list.toArray(new Field[list.size()]);
    }

    @NotNull
    public Field[] getMembers(Class<? extends Annotation> association)
    {
        return fieldMapping.get(association);
    }

    public Class<? extends Annotation> getAssociation(Field field)
    {
        return associationMapping.get(field);
    }

    @NotNull
    public Set<Field> getAssociations()
    {
        return associationMapping.keySet();
    }

    public InvalidDataException validateId(Object data)
    {
        Field idField = getIdField();
        try
        {
            if (idField.get(data) == null && !idField.getAnnotation(Id.class).autoGenerate())
            {
                return new InvalidDataException("Id field in " + entity.getSimpleName() + " must declare 'autoGenerate = true' to hold the value null");
            }
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public InvalidDataException validateData(Object data)
    {
        try
        {
            for (Field field : this.columns)
            {
                if (field == getIdField())
                {
                    InvalidDataException exception = validateId(data);
                    if (exception != null)
                    {
                        return exception;
                    }
                }
                if (field.get(data) == null && !field.getAnnotation(Column.class).nullable())
                {
                    return new InvalidDataException(field.getName() + " in " + entity.getSimpleName() + " must declare 'nullable = true' to hold the value null");
                }
            }
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * this check will guarantee that
     * 1. all fields with 'nullable = false' must have non-null value
     * 2. id field with 'autoGenerated = false' must have non-null value
     */
    public InvalidDataException validate(Object data)
    {
        InvalidDataException exception = validateId(data);
        if (exception == null)
        {
            exception = validateData(data);
        }
        return exception;
    }

    public PreparedStatement getInsertStatement(Connection connection) throws SQLException
    {
        if (isIdAutoGenerated())
        {
            String[] idColumns = new String[]{columnMapping.get(getIdField().getName())};
            return connection.prepareStatement(insert, idColumns);
        }
        return connection.prepareStatement(insert);
    }

    public PreparedStatement getUpdateStatement(Connection connection) throws SQLException
    {
        return connection.prepareStatement(update);
    }

    public PreparedStatement getDeleteStatement(Connection connection) throws SQLException
    {
        return connection.prepareStatement(delete);
    }

    public PreparedStatement getUpdateForeignKeyStatement(Connection connection) throws SQLException
    {
        return updateForeignKey == null ? null : connection.prepareStatement(updateForeignKey);
    }

    public PreparedStatement getUpdateNull(Connection connection) throws SQLException
    {
        return updateNull == null ? null : connection.prepareStatement(updateNull);
    }

    public PreparedStatement getExistStatement(Connection connection) throws SQLException
    {
        return connection.prepareStatement(exist);
    }

    public boolean isLeaf()
    {
        return isLeaf;
    }

   /* public void traverseAll(Consumer consumer)
    {
        for (int i = 0; i < columns.length; ++i)
        {
            consumer.preSet(columns[i], i);
        }
    }

    public void traverse(Consumer consumer)
    {
        if (isIdAutoGenerated())
        {
            for (int i = 0; i < columns.length; )
            {
                if (i != idPos)
                {
                    consumer.preSet(columns[i], i);
                    ++i;
                }
            }
        }
        else
        {
            traverseAll(consumer);
        }
    }

    public interface Consumer
    {
        void preSet(Field field, int i);
    }*/
   /* public void traverseAll(Consumer consumer)
    {
        for (Field field : columns)
        {
            consumer.preSet(field);
        }
    }

    public void traverse(Consumer consumer)
    {
        if (!isIdAutoGenerated())
        {
            consumer.preSet(getIdField());
        }
        for (Field field : columns)
        {
            consumer.preSet(field);
        }
    }

    public interface Consumer
    {
        void preSet(Field field);
    }

    public interface IndexConsumer
    {
        void preSet(Field field, int i);
    }*/
}

