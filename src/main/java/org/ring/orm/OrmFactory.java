package org.ring.orm;

import org.ring.dml.query.converter.TypeConverter;
import org.ring.dml.transaction.save.converter.IdConverter;
import org.ring.dml.transaction.save.AbstractPersistence;
import org.ring.exporter.generator.ModelGenerator;
import org.ring.orm.parser.GeneratorParser;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import org.ring.orm.parser.ConverterParser;
import org.ring.orm.parser.FunctionParser;
import org.ring.orm.parser.PersistenceParser;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Created by quanle on 5/12/2017.
 */
public class OrmFactory
{
    private static String url;
    private static String driver;
    private static Properties properties;

    public static void config(String uri) throws ParserConfigurationException, IOException, SAXException, SQLException, ClassNotFoundException
    {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = builder.parse(uri);
        document.getDocumentElement().normalize();
        config(document);
    }

    public static void config(File file) throws ParserConfigurationException, IOException, SAXException, SQLException, ClassNotFoundException
    {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = builder.parse(file);
        document.getDocumentElement().normalize();
        config(document);
    }

    private static void config(Document document) throws IOException, SAXException, ParserConfigurationException, ClassNotFoundException, SQLException
    {
        Element databaseNode = (Element) document.getElementsByTagName("database").item(0);

        driver = databaseNode.getElementsByTagName("driver").item(0).getTextContent();
        url = databaseNode.getElementsByTagName("url").item(0).getTextContent();

        NodeList propertiesNode = databaseNode.getElementsByTagName("properties");
        if (propertiesNode.getLength() == 1)
        {
            properties = new Properties();
            NodeList propertiesList = propertiesNode.item(0).getChildNodes();
            for (int i = 0; i < propertiesList.getLength(); i++)
            {
                Node property = propertiesList.item(i);
                if (property.getNodeType() == Node.ELEMENT_NODE)
                {
                    properties.put(property.getNodeName(), property.getTextContent());
                }
            }
        }

        testConnection();
        configParser();
    }

    private static void testConnection() throws ClassNotFoundException, SQLException
    {
        Class.forName(driver);
        Connection connection;
        if (properties == null)
        {
            connection = DriverManager.getConnection(url);
        }
        else
        {
            connection = DriverManager.getConnection(url, properties);
        }
        connection.close();
    }


    private static void configParser() throws ParserConfigurationException, IOException, SAXException
    {
        InputStream stream = OrmFactory.class.getResourceAsStream("/driver handler.xml");
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = builder.parse(stream);
        document.getDocumentElement().normalize();

        NodeList nodeList = document.getElementsByTagName("driver");
        for (int i = 0; i < nodeList.getLength(); i++)
        {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE)
            {
                Element driverElement = (Element) node;
                String[] driverName = driverElement.getAttribute("name").split(" ");
                for (String name : driverName)
                {
                    if (driver.equals(name))
                    {
                        ConverterParser.parse(driverElement.getElementsByTagName("converter").item(0).getChildNodes());
                        FunctionParser.parse(driverElement.getElementsByTagName("function").item(0).getChildNodes());
                        PersistenceParser.parse(driverElement.getElementsByTagName("persistence-handler").item(0).getChildNodes());
                        GeneratorParser.parse(driverElement.getElementsByTagName("generator").item(0));
                        return;
                    }
                }
            }
        }
    }

    public static Connection getConnection()
    {
        try
        {
            if (properties == null)
            {
                return DriverManager.getConnection(url);
            }
            return DriverManager.getConnection(url, properties);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static IdConverter getIdConverter()
    {
        return (IdConverter) ConverterParser.getConverter("id-converter");
    }

    public static TypeConverter getTypeConverter()
    {
        return (TypeConverter) ConverterParser.getConverter("type-converter");
    }

    public static String getIfNullFunction()
    {
        return FunctionParser.getFunction("if-null");
    }

    public static AbstractPersistence newPersistence(boolean autoGeneratedId)
    {
        try
        {
            return PersistenceParser.getHandler(autoGeneratedId);
        }
        catch (InstantiationException | IllegalAccessException | InvocationTargetException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static ModelGenerator getGenerator()
    {
        return GeneratorParser.generator;
    }
}
