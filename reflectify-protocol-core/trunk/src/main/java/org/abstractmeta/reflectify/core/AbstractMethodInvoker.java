package org.abstractmeta.reflectify.core;


import org.abstractmeta.reflectify.ParameterSetter;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

public abstract class AbstractMethodInvoker {

    private final Type[] genericArgumentTypes;
    private final Class[] argumentTypes;
    private final Type genericResultType;
    private final Class resultType;


    @SuppressWarnings("unchecked")
    protected AbstractMethodInvoker(Class clazz, String methodName, Class... argumentTypes) {
        try {

            Method method = clazz.getMethod(methodName, argumentTypes);
            this.argumentTypes = argumentTypes;
            this.genericArgumentTypes = method.getGenericParameterTypes();
            this.genericResultType = method.getGenericReturnType();
            this.resultType = method.getReturnType();
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Failed to lookup method " + methodName, e);
        }
    }

    public Class[] getParameterTypes() {
        return argumentTypes;
    }

    public Type[] getGenericParameterTypes() {
        return genericArgumentTypes;
    }

    public Type getResultGenericType() {
        return genericResultType;
    }

    public Class getResultType() {
        return resultType;
    }

    public abstract <T> ParameterSetter<T> getParameterSetter(Class<T> argumentType, int argumentIndex);

    public ParameterSetter<Object> getParameterSetter(int argumentIndex) {
        return getParameterSetter(Object.class, argumentIndex);
    }

}
