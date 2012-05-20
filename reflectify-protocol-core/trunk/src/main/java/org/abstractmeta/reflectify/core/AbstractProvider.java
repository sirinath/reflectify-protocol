package org.abstractmeta.reflectify.core;


import org.abstractmeta.reflectify.ParameterSetter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;

public abstract class AbstractProvider {

    private final Type[] genericParameterTypes;
    private final Class[] parameterTypes;


    @SuppressWarnings("unchecked")
    protected AbstractProvider(Class clazz, Class... parameterTypes) {
        try {

            Constructor constructor = clazz.getConstructor(parameterTypes);
            this.parameterTypes = parameterTypes;
            this.genericParameterTypes = constructor.getGenericParameterTypes();
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Failed to load constructor " + clazz.getSimpleName(), e);
        }
    }


    public Type[] getGenericParameterTypes() {
        return genericParameterTypes;
    }

    public Class [] getParameterTypes() {
        return parameterTypes;
    }
    
    public abstract <T> ParameterSetter<T> getParameterSetter(Class<T> argumentType, int argumentIndex);

    public ParameterSetter<Object> getParameterSetter(int argumentIndex) {
        return getParameterSetter(Object.class, argumentIndex);
    }

}
