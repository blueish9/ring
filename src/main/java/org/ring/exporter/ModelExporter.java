package org.ring.exporter;

import org.ring.entity.EntityManager;
import org.ring.entity.Mapper;
import org.ring.meta.annotation.entity.Column;
import org.ring.meta.annotation.relationship.ManyToMany;
import org.ring.meta.annotation.relationship.ManyToOne;
import org.ring.meta.annotation.relationship.OneToOne;
import org.ring.exporter.generator.ModelGenerator;
import org.ring.exporter.generator.SqliteGenerator;
import org.ring.orm.OrmFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ModelExporter
{
    private List<Class<?>> listModel = new ArrayList<>();
    private StringBuilder ddlBuilder = new StringBuilder();
    private ModelGenerator generator = OrmFactory.getGenerator();

    public ModelExporter()
    {
    }

    public ModelExporter(List<Class<?>> listModel)
    {
        this.listModel = listModel;
    }

    public void addModel(Class<?> c)
    {
        this.listModel.add(c);
    }

    public String export()
    {
        Mapper mapper;
        for (Class<?> model : listModel)
        {
            StringBuilder temp = new StringBuilder();
            temp.append("CREATE TABLE ");
            mapper = EntityManager.getMapper(model);

            temp.append(mapper.getTable()).append(" (");
            for (Field f : mapper.getColumns())
            {
                temp.append(f.getAnnotation(Column.class).name())
                        .append(" ")
                        .append(generator.getDataType(f))
                        .append(" ")
                        .append(generator.getNullable(f))
                        .append(" ")
                        .append(generator.getAutoGenerate(f))
                        .append(",");
            }

            for (Field f : mapper.getMembers(OneToOne.class))
            {
                temp.append(f.getAnnotation(OneToOne.class).foreignKey())
                        .append(" ")
                        .append(generator.getForeignType(f.getAnnotation(OneToOne.class).entity()))
                        .append(",");
            }

            for (Field f : mapper.getMembers(ManyToOne.class))
            {
                temp.append(f.getAnnotation(ManyToOne.class).foreignKey())
                        .append(" ")
                        .append(generator.getForeignType(f.getAnnotation(ManyToOne.class).entity()))
                        .append(",");
            }

//            for (Field f : mapper.getMembers(OneToMany.class))
//            {
////                if (!temp.toString().toLowerCase().contains(f.getAnnotation(OneToMany.class).referencedColumn().toLowerCase())) {
//                    temp.append(f.getAnnotation(OneToMany.class).referencedColumn())
//                            .append(" ")
//                            .append(generator.getForeignType(f.getAnnotation(OneToMany.class).entity()))
//                            .append(",");
////                }
//            }

            temp.append("primary key (").append(mapper.getIdField().getAnnotation(Column.class).name()).append("),");

            temp.delete(temp.length() - 1, temp.length());
            temp.append(")");

            ddlBuilder.append(temp.toString()).append(";\n");
        }
        extraTable();
        if(!(generator instanceof SqliteGenerator)){

            alterTable();

            extraTableFK();

        }
        //System.out.println(ddlBuilder.toString());
        return ddlBuilder.toString();
    }

    void alterTable() {
        Mapper mapper;

        for (Class<?> model : listModel)
        {
            StringBuilder temp = new StringBuilder();
            mapper = EntityManager.getMapper(model);
            if (mapper.getMembers(OneToOne.class).length > 0)
            {

                for (Field f : mapper.getMembers(OneToOne.class))
                {
                    temp.append("ALTER TABLE ");
                    temp.append(mapper.getTable()).append(" ADD ");
                    temp.append("FOREIGN KEY (")
                            .append(f.getAnnotation(OneToOne.class).foreignKey())
                            .append(") references ")
                            .append(generator.getTableName(f.getAnnotation(OneToOne.class).entity()))
                            .append("(")
                            .append(generator.getPrimaryKey(f.getAnnotation(OneToOne.class).entity()))
                            .append("),");

                    temp.delete(temp.length() - 1, temp.length());
                    temp.append("");

                    ddlBuilder.append(temp.toString()).append(";\n");
                }
            }
        }

        for (Class<?> model : listModel)
        {
            StringBuilder temp = new StringBuilder();
            mapper = EntityManager.getMapper(model);
            if (mapper.getMembers(ManyToOne.class).length > 0)
            {
                for (Field f : mapper.getMembers(ManyToOne.class))
                {
                    temp.append("ALTER TABLE ");

                    temp.append(mapper.getTable()).append(" ");

                    temp.append("ADD FOREIGN KEY (")
                            .append(f.getAnnotation(ManyToOne.class).foreignKey())
                            .append(") references ")
                            .append(generator.getTableName(f.getAnnotation(ManyToOne.class).entity()))
                            .append("(")
                            .append(generator.getPrimaryKey(f.getAnnotation(ManyToOne.class).entity()))
                            .append("),");

                    temp.delete(temp.length() - 1, temp.length());
                    temp.append("");

                    ddlBuilder.append(temp.toString()).append(";\n");
                }
            }
        }

//        for (Class<?> model : listModel)
//        {
//            StringBuilder temp = new StringBuilder();
//            mapper = EntityManager.getMapper(model);
//            if (mapper.getMembers(OneToMany.class).length > 0)
//            {
//                for (Field f : mapper.getMembers(OneToMany.class))
//                {
//                    temp.append("ALTER TABLE ");
//
//                    temp.append(generator.getTableName(f.getAnnotation(OneToMany.class).entity())).append(" ");
//
//                    temp.append("ADD FOREIGN KEY (")
//                            .append(f.getAnnotation(OneToMany.class).referencedColumn())
//                            .append(") references ")
//                            .append(mapper.getTable())
//                            .append("(")
//                            .append(mapper.getIdField().getAnnotation(Column.class).name())
//                            .append("),");
//
//                    temp.delete(temp.length() - 1, temp.length());
//                    temp.append("");
//
//                    ddlBuilder.append(temp.toString()).append(";\n");
//                }
//            }
//        }

    }

    void extraTable() {
        List<String> tables = new ArrayList<>();
        for (Class<?> model : listModel)
        {
            Mapper mapper = EntityManager.getMapper(model);
            for (Field f : mapper.getMembers(ManyToMany.class))
            {
                String temp = f.getAnnotation(ManyToMany.class).joinTable();
                if (!temp.isEmpty() && !tables.contains(temp))
                {
                    tables.add(temp);
                }
            }
        }

        for (String table : tables)
        {
            StringBuilder extra = new StringBuilder();
            extra.append("CREATE TABLE ")
                    .append(table)
                    .append("(")
                    .append(generator.getExtraTablePK())
                    .append(',')
            ;

            for (Class<?> model : listModel)
            {
                Mapper mapper = EntityManager.getMapper(model);
                for (Field f : mapper.getMembers(ManyToMany.class))
                {
                    if (f.getAnnotation(ManyToMany.class).joinTable().equals(table))
                    {
                        extra.append(f.getAnnotation(ManyToMany.class).foreignKey())
                                .append(" ")
                                .append(generator.getForeignType(f.getAnnotation(ManyToMany.class).entity()))
                                .append(",");
                    }
                }
            }

            extra.delete(extra.length() - 1, extra.length());
            extra.append(")");
            ddlBuilder.append(extra.toString()).append(";\n");
        }
    }

    void extraTableFK(){
        List<String> tables = new ArrayList<>();

        for (Class<?> model : listModel)
        {
            Mapper mapper = EntityManager.getMapper(model);
            for (Field f : mapper.getMembers(ManyToMany.class))
            {
                String temp = f.getAnnotation(ManyToMany.class).joinTable();
                if (!temp.isEmpty() && !tables.contains(temp))
                {
                    tables.add(temp);
                }
            }
        }

        List<String> extraTable = new ArrayList<>();

        for (String table : tables)
        {
            for (Class<?> model : listModel)
            {
                Mapper mapper = EntityManager.getMapper(model);
                for (Field f : mapper.getMembers(ManyToMany.class))
                {
                    if (f.getAnnotation(ManyToMany.class).joinTable().equals(table))
                    {
                        StringBuilder extra = new StringBuilder();
                        extra.append("ALTER TABLE ")
                                .append(table)
                                .append(" ");
                        extra.append("ADD FOREIGN KEY (")
                                .append(f.getAnnotation(ManyToMany.class).foreignKey())
                                .append(") references ")
                                .append(f.getAnnotation(ManyToMany.class).entity().getSimpleName())
                                .append("(")
                                .append(generator.getPrimaryKey(f.getAnnotation(ManyToMany.class).entity()))
                                .append("),");

                        extra.delete(extra.length() - 1, extra.length());
                        extra.append("");
                        ddlBuilder.append(extra.toString()).append(";\n");
                    }
                }
            }
        }
    }
}
