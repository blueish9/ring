package org.ring.dml.statement.interceptor;

import org.ring.entity.Mapper;
import org.ring.meta.annotation.entity.Column;
import org.ring.meta.annotation.entity.Getter;
import org.ring.meta.annotation.relationship.ManyToOne;
import org.ring.meta.annotation.relationship.OneToOne;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by quanle on 6/16/2017.
 */
public class GetterInterceptor implements MethodInterceptor
{
    HashMap<String, String> getterMap;
    ArrayList<String> calledList;

    public GetterInterceptor(Mapper mapper)
    {
        calledList = new ArrayList<>();
        getterMap = new HashMap<>();
        Class<?> entity = mapper.getEntity();
        add(entity, mapper.getColumns(), field -> field.getAnnotation(Column.class).name());
        add(entity, mapper.getMembers(OneToOne.class), field -> field.getAnnotation(OneToOne.class).foreignKey());
        add(entity, mapper.getMembers(ManyToOne.class), field -> field.getAnnotation(ManyToOne.class).foreignKey());
    }

    @Override
    public Object intercept(Object self, Method method, Object[] args, MethodProxy methodProxy) throws Throwable
    {
        String column = getterMap.get(method.getName());
        if (column != null)
        {
            calledList.add(column);
        }
        return methodProxy.invokeSuper(self, args);
    }

    private void add(Class<?> entity, Field[] fields, GetColumn getColumn)
    {
        PropertyDescriptor pd;
        Method getterMethod;
        for (Field field : fields)
        {
            try
            {
                pd = new PropertyDescriptor(field.getName(), entity);
                getterMethod = pd.getReadMethod();
                getterMap.put(getterMethod.getName(), getColumn.invoke(field));
            }
            catch (IntrospectionException e)
            {
                Getter getter = field.getAnnotation(Getter.class);
                if (getter != null)
                {
                    getterMap.put(getter.value(), getColumn.invoke(field));
                }
                else
                {
                    System.err.println(field.getName() + " getter not found");
                }
            }
        }
    }

    private interface GetColumn
    {
        String invoke(Field field);
    }

    public ArrayList<String> getCalledList()
    {
        return calledList;
    }
}
