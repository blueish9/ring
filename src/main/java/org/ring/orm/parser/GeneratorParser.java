package org.ring.orm.parser;

import org.ring.exporter.generator.ModelGenerator;
import org.w3c.dom.Node;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by quanle on 7/3/2017.
 */
public class GeneratorParser
{
    public static ModelGenerator generator;

    public static void parse(Node generatorNode)
    {
        try
        {
            Class<ModelGenerator> generatorClass = (Class<ModelGenerator>) Class.forName(generatorNode.getTextContent());
            Constructor<ModelGenerator> constructor = generatorClass.getConstructor();
            generator = constructor.newInstance();
        }
        catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException | ClassNotFoundException e)
        {
            e.printStackTrace();
        }
    }
}
