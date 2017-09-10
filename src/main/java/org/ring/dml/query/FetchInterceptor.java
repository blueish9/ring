package org.ring.dml.query;

import org.ring.entity.EntityManager;
import org.ring.entity.Mapper;
import org.ring.meta.annotation.entity.Getter;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.ring.dml.query.association.AssociationQuery;
import org.ring.dml.query.association.QueryFactory;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.HashMap;

/**
 * Created by quanle on 5/15/2017.
 */
public class FetchInterceptor implements MethodInterceptor
{
    private Mapper mapper;
    private HashMap<String, Field> getterMap;

    public FetchInterceptor(Class<?> entity)
    {
        getterMap = new HashMap<>();

        PropertyDescriptor pd;
        Method getterMethod;
        mapper = EntityManager.getMapper(entity);
        for (Field field : mapper.getAssociations())
        {
            try
            {
                pd = new PropertyDescriptor(field.getName(), entity);
                getterMethod = pd.getReadMethod();
                getterMap.put(getterMethod.getName(), field);
            }
            catch (IntrospectionException e)
            {
                Getter getter = field.getAnnotation(Getter.class);
                if (getter != null)
                {
                    getterMap.put(getter.value(), field);
                }
                else
                {
                    System.err.println(field.getName() + " getter not found");
                }
            }
        }
    }

    public Object intercept(Object self, Method method, Object[] args, MethodProxy methodProxy) throws Throwable
    {
        Field field = getterMap.get(method.getName());
        if (field != null)
        {
            Class<? extends Annotation> association = mapper.getAssociation(field);
            if (association != null)
            {
                AssociationQuery query = QueryFactory.initQuery(association, field, self, mapper.getEntity());
                if (query.execute())
                {
                    getterMap.remove(method.getName());
                }
            }
        }
        return methodProxy.invokeSuper(self, args);
    }
}
