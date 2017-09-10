package org.ring.oql;

/**
 * Created by quanle on 5/11/2017.
 */
public class StatementUtils
{
    public static String trim(String statement)
    {
        int end = statement.length() - 1;
        if (statement.charAt(end) == ',')
        {
            return statement.substring(0, end);
        }
        return statement;
    }
}
