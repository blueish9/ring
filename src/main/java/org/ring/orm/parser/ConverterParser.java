package org.ring.orm.parser;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

/**
 * Created by quanle on 6/21/2017.
 */
public class ConverterParser
{
    private static HashMap<String, Object> converterMap = new HashMap<>();

    public static void parse(NodeList converterNode)
    {
        try
        {
            for (int i = 0; i < converterNode.getLength(); i++)
            {
                Node function = converterNode.item(i);
                if (function.getNodeType() == Node.ELEMENT_NODE)
                {
                    Class<?> type = Class.forName(function.getTextContent());
                    Constructor<?> constructor = type.getConstructor();
                    converterMap.put(((Element) function).getTagName(), constructor.newInstance());
                }
            }
        }
        catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e)
        {
            e.printStackTrace();
        }
    }

    public static Object getConverter(String name)
    {
        return converterMap.get(name);
    }
}
