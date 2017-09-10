package org.ring.oql.parser;

import org.ring.entity.EntityManager;
import org.ring.entity.Mapper;
import org.ring.meta.annotation.relationship.ManyToMany;
import org.ring.meta.annotation.relationship.ManyToOne;
import org.ring.meta.annotation.relationship.OneToMany;
import org.ring.meta.annotation.relationship.OneToOne;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by quanle on 5/9/2017.
 */
public class JoinParser extends Parser
{
    private HashMap<String, Class<?>> aliasMap;
    private HashMap<String, ManyToMany> joinAliasMap;
    private int aliasIndex = 1;

    private HashMap<String, String> listJoin = new HashMap<>();

    public void add(String x, String y)
    {
        listJoin.put(x, y);
    }

    public HashMap<String, String> getListJoin()
    {
        return listJoin;
    }

    public JoinParser(HashMap<String, Class<?>> aliasMap)
    {
        this.aliasMap = aliasMap;
        joinAliasMap = new HashMap<>();
    }

    @Override
    public String parseField(String... operands) throws NoSuchFieldException
    {
        String[] parts = operands[0].split("\\.");
        if (parts.length != 2)
        {
            return operands[0];
        }

        if (operands.length == 2)
        {
            String owner = parts[0];
            Class<?> ownerType = aliasMap.get(owner);
            Mapper ownerMapper = EntityManager.getMapper(ownerType);
            Field memberField = ownerType.getDeclaredField(parts[1]);
            Class<?> type = ownerMapper.getAssociation(memberField);
            if (type == OneToOne.class)
            {
                return parseForeignKey(owner, memberField.getAnnotation(OneToOne.class).foreignKey(), operands[1]);
            }
            if (type == ManyToOne.class)
            {
                return parseForeignKey(owner, memberField.getAnnotation(ManyToOne.class).foreignKey(), operands[1]);
            }
            if (type == OneToMany.class)
            {
                return parseOneToMany(owner, ownerMapper, operands[1], memberField.getAnnotation(OneToMany.class).referencedColumn());
            }
            if (type == ManyToMany.class)
            {
                return parseJoin(owner, ownerMapper, operands[1], memberField.getAnnotation(ManyToMany.class));
            }
            return null;
        }
        else
        {
            String alias = parts[0];
            String field = parts[1];
            if (field.equals("*"))
            {
                return alias + "." + field;
            }

            Class<?> entity = aliasMap.get(alias);
            return alias + "." + EntityManager.getColumn(entity, field);
        }
    }

    String parseForeignKey(String owner, String foreignKey, String member) throws NoSuchFieldException
    {
        Class<?> memberType = aliasMap.get(member);
        Mapper memberMapper = EntityManager.getMapper(memberType);
        String memberId = memberMapper.getPrimaryKey();
        return String.format("%s.%s = %s.%s", owner, foreignKey, member, memberId);
    }

    String parseOneToMany(String owner, Mapper ownerMapper, String member, String foreignKey)
    {
        String ownerId = ownerMapper.getPrimaryKey();
        return String.format("%s.%s = %s.%s", owner, ownerId, member, foreignKey);
    }

    String parseJoin(String owner, Mapper ownerMapper, String member, ManyToMany association)
    {
        Class<?> memberType = aliasMap.get(member);
        Mapper memberMapper = EntityManager.getMapper(memberType);
        String memberId = memberMapper.getPrimaryKey();
        String ownerId = ownerMapper.getPrimaryKey();
        String joinAlias = getJoinAlias(association);
        return String.format("%s.%s = %s.%s and %s.%s = %s.%s",
                owner, ownerId, joinAlias, association.referencedColumn(),
                member, memberId, joinAlias, association.foreignKey());
    }

    String getJoinAlias(ManyToMany association)
    {
        String alias = association.joinTable() + aliasIndex;
        while (aliasMap.containsKey(alias) || joinAliasMap.containsKey(alias))
        {
            aliasIndex++;
            alias = association.joinTable() + aliasIndex;
        }
        joinAliasMap.put(alias, association);
        return alias;
    }

    @Override
    public String getJoinTable()
    {
        if (joinAliasMap.size() > 0)
        {
            String from = "";
            for (Map.Entry<String, ManyToMany> pair : joinAliasMap.entrySet())
            {
                from += pair.getValue().joinTable() + " " + pair.getKey() + ",";
            }
            return from;
        }
        return null;
    }

    public HashMap<String, ManyToMany> getJoinAliasMap()
    {
        return joinAliasMap;
    }
}
