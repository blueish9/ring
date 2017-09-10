package org.ring.dml.statement.later;

import org.ring.dml.statement.interceptor.SetterInterceptor;
import org.ring.entity.Mapper;
import org.ring.oql.expression.Expressible;
import org.ring.oql.parser.Parser;
import org.ring.orm.OrmFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by quanle on 6/17/2017.
 */
public class UpdateStatement
{
  private Expressible whereExpression;
  private SetterInterceptor interceptor;
  private Mapper mapper;
  private Parser whereParser;

    UpdateStatement(Mapper mapper, Expressible whereExpression, SetterInterceptor interceptor, Parser whereParser)
    {
        this.mapper = mapper;
        this.whereExpression = whereExpression;
        this.interceptor = interceptor;
        this.whereParser = whereParser;
    }

    public int update()
    {
        try
        {
            String set = "";
            ArrayList values = new ArrayList();
            HashMap<String, Object> setValue = interceptor.getValueMap();
            for (Map.Entry<String, Object> entry : setValue.entrySet())
            {
                set += entry.getKey() + " = ?,";
                values.add(entry.getValue());
            }

            String where = whereExpression.print(whereParser);

            Connection connection = OrmFactory.getConnection();
            String dml = String.format("update %s set %s where %s",
                    mapper.getTable(), set.substring(0, set.length() - 1), where);
            PreparedStatement statement = connection.prepareStatement(dml);
            int i = 1;
            for (Object data : values)
            {
                statement.setObject(i, data);
                i++;
            }
            for (Object data : whereParser.getValues())
            {
                statement.setObject(i, data);
                i++;
            }
            int rows =  statement.executeUpdate();
            statement.close();
            connection.close();
            return rows;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        catch (NoSuchFieldException e)
        {
            e.printStackTrace();
        }
        return 0;
    }
}
