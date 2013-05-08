package com.thoughtworks.orm.core;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collection;

import static com.thoughtworks.orm.util.Lang.getId;
import static com.thoughtworks.orm.util.Lang.info;

public class AssociationInterceptor implements MethodInterceptor {
    private SessionFactory sessionFactory;

    public AssociationInterceptor(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Object intercept(Object object, Method method, Object[] params, MethodProxy methodProxy) throws Throwable {
        Object result;
        Class returnType = method.getReturnType();
        if (isCollection(returnType)) {
            info("start to load associated collections.");
            result = sessionFactory.where(foreignKey(object) + " = ?", Arrays.asList(getId(object)).toArray(), resolveTargetClass(method));
        } else {
            //why method.invoke not working?
            result = methodProxy.invokeSuper(object, params);
        }

        return result;
    }

    private Class resolveTargetClass(Method method) {
        return (Class) ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];
    }

    private boolean isCollection(Class clazz) {
        return Arrays.asList(clazz.getInterfaces()).contains(Collection.class);
    }

    private String foreignKey(Object model) {
        String enhancedSimpleName = model.getClass().getSimpleName();
        return enhancedSimpleName.substring(0, enhancedSimpleName.indexOf("$$")).toLowerCase() + "_id";
    }
}
