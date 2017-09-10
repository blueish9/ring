package org.ring.dml.statement.later;

import org.ring.entity.Mapper;
import org.ring.oql.StatementUtils;
import org.ring.oql.expression.Expressible;
import org.ring.oql.parser.Parser;
import org.ring.orm.OrmFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by quanle on 6/23/2017.
 */
public class SelectStatement
{
    private Expressible whereExpression;
    private ArrayList<String> projections;
    private Mapper mapper;
    private Parser whereParser;
    private Aggregator aggregator;

    public SelectStatement(Expressible whereExpression, Mapper mapper, Parser whereParser, Aggregator aggregator)
    {
        this.whereExpression = whereExpression;
        this.mapper = mapper;
        this.whereParser = whereParser;
        this.aggregator = aggregator;
    }

    SelectStatement(Mapper mapper, Expressible whereExpression, ArrayList<String> projections, Parser whereParser)
    {
        this.mapper = mapper;
        this.whereExpression = whereExpression;
        this.projections = projections;
        this.whereParser = whereParser;
    }

    public ArrayList<HashMap<String, Object>> query()
    {
        try
        {
            String select = "";
            for (String column : projections)
            {
                select += column + ",";
            }
            String where = whereExpression.print(whereParser);
            String sql = String.format("select %s from %s where %s", StatementUtils.trim(select), mapper.getTable(), where);

            Connection connection = OrmFactory.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
            int i = 1;
            for (Object value : whereParser.getValues())
            {
                statement.setObject(i, value);
                i++;
            }
            ResultSet rs = statement.executeQuery();
            ArrayList<HashMap<String, Object>> list = new ArrayList<>();
            // get list hashmap from phu'

            rs.close();
            statement.close();
            connection.close();

            return list;
        }
        catch (SQLException | NoSuchFieldException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
