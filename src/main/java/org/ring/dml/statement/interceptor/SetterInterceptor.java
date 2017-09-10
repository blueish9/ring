package org.ring.dml.statement.interceptor;

import org.ring.entity.Mapper;
import org.ring.meta.annotation.entity.Column;
import org.ring.meta.annotation.entity.Setter;
import org.ring.meta.annotation.relationship.ManyToOne;
import org.ring.meta.annotation.relationship.OneToOne;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * Created by quanle on 6/17/2017.
 */
public class SetterInterceptor implements MethodInterceptor
{
    HashMap<String, String> setterMap;
    HashMap<String, Object> valueMap;

    public SetterInterceptor(Mapper mapper)
    {
        setterMap = new HashMap<>();
        valueMap = new HashMap<>();
        Class<?> entity = mapper.getEntity();
        add(entity, mapper.getColumns(), field -> field.getAnnotation(Column.class).name());
        add(entity, mapper.getMembers(OneToOne.class), field -> field.getAnnotation(OneToOne.class).foreignKey());
        add(entity, mapper.getMembers(ManyToOne.class), field -> field.getAnnotation(ManyToOne.class).foreignKey());
    }

    @Override
    public String intercept(Object self, Method method, Object[] args, MethodProxy methodProxy) throws Throwable
    {
        String column = setterMap.get(method.getName());
        valueMap.put(column, args[0]);
        return null;
    }

    private void add(Class<?> entity, Field[] fields, GetColumn getColumn)
    {
        PropertyDescriptor pd;
        Method setterMethod;
        for (Field field : fields)
        {
            try
            {
                pd = new PropertyDescriptor(field.getName(), entity);
                setterMethod = pd.getWriteMethod();
                setterMap.put(setterMethod.getName(), getColumn.invoke(field));
            }
            catch (IntrospectionException e)
            {
                Setter setter = field.getAnnotation(Setter.class);
                if (setter != null)
                {
                    setterMap.put(setter.value(), getColumn.invoke(field));
                }
                else
                {
                    System.err.println(field.getName() + " setter not found");
                }
            }
        }
    }

    private interface GetColumn
    {
        String invoke(Field field);
    }

    public HashMap<String, Object> getValueMap()
    {
        return valueMap;
    }
}
