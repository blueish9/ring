package org.ring.orm.parser;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;

/**
 * Created by quanle on 6/21/2017.
 */
public class FunctionParser
{
    private static HashMap<String, String> functionMap = new HashMap<>();

    public static void parse(NodeList functionNode)
    {
        for (int i = 0; i < functionNode.getLength(); i++)
        {
            Node function = functionNode.item(i);
            if (function.getNodeType() == Node.ELEMENT_NODE)
            {
                functionMap.put(((Element) function).getTagName(), function.getTextContent());
            }
        }
    }

    public static String getFunction(String name)
    {
        return functionMap.get(name);
    }
}
