package com.thoughtworks.orm.core;


import com.google.common.collect.ImmutableSet;
import com.thoughtworks.orm.annotations.HasMany;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static com.thoughtworks.orm.util.Lang.getId;
import static com.thoughtworks.orm.util.Lang.info;
import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;

public class AssociationInterceptor implements MethodInterceptor {
    private SessionFactory sessionFactory;

    public AssociationInterceptor(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Object intercept(Object object, Method method, Object[] params, MethodProxy methodProxy) throws Throwable {
        if (isGetter(method)) {
            Field field = associationField(method);
            if (isAssociation(field)) {
                info("start to load associated collections.");
                List result = sessionFactory.where(foreignKey(object) + " = ?", Arrays.asList(getId(object)).toArray(), targetClass(field));

                if (method.getReturnType().equals(Set.class)) {
                    return ImmutableSet.copyOf(result);
                } else {
                    return result;
                }


            }
        }
        //why method.invoke not working?
        return methodProxy.invokeSuper(object, params);
    }

    private boolean isAssociation(Field field) {
        return null != field && field.isAnnotationPresent(HasMany.class);
    }

    private Class targetClass(Field field) {
        return (Class) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
    }

    private Field associationField(Method method) {
        Field field = null;
        Class clazz = method.getDeclaringClass();
        String fieldName = UPPER_CAMEL.to(LOWER_CAMEL, method.getName().replace("get", ""));
        try {
            field = clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            info(String.format("cannot find field for method %s, errors : %s", method.getName(), e.getMessage()));
        }
        return field;
    }

    private boolean isGetter(Method method) {
        return method.getName().startsWith("get");
    }

    private String foreignKey(Object model) {
        String enhancedSimpleName = model.getClass().getSimpleName();
        return enhancedSimpleName.substring(0, enhancedSimpleName.indexOf("$$")).toLowerCase() + "_id";
    }
}
