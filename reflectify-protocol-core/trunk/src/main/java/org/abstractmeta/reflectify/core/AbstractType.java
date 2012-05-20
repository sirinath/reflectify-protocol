package org.abstractmeta.reflectify.core;

import org.abstractmeta.reflectify.ParameterSetter;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Represents AbstractType
 *
 * @author Adrian Witas
 */
public abstract class AbstractType {

    private final RuntimeException invalidClassDefinition;
    private final Type genericType;
    private final Class type;


    public AbstractType() {
        invalidClassDefinition =  new IllegalStateException("Class " + getClass() + " should implement XXX<T> generic type");
        Type[] genericInterfaces = this.getClass().getGenericInterfaces();
        if (genericInterfaces == null || genericInterfaces.length == 0) {
            throw invalidClassDefinition;
        }
        ParameterizedType parameterizedType = getParameterizedType(genericInterfaces[0]);
        if(parameterizedType.getActualTypeArguments().length == 0) {
           throw invalidClassDefinition;
        }
        genericType = parameterizedType.getActualTypeArguments()[0];
        type = getRawClass(genericType);
    }

    /**
     * Returns a method argument setter
     *
     * @param argumentType  argument Type
     * @param argumentIndex method argument index starting from 0
     * @return argument setter
     */
    public <T> ParameterSetter<T> getParameterSetter(Class<T> argumentType, int argumentIndex) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a method argument setter
     *
     * @param argumentIndex method argument index starting from 0
     * @return argument setter
     */
    public ParameterSetter<Object> getParameterSetter(int argumentIndex) {
        return getParameterSetter(Object.class, argumentIndex);
    }

    public Type[] getGenericParameterTypes() {
        return new Type[]{};
    }

    public Class[] getParameterTypes() {
        return new Class[]{};
    }


    /**
     * Returns method result type.
     *
     * @return method result type.
     */
    public Type getResultGenericType() {
        return genericType;
    }

    public Class getResultType() {
        return type;
    }

    protected ParameterizedType getParameterizedType(Type type) {
        if (type instanceof ParameterizedType) {
            return ParameterizedType.class.cast(type);
        }
        return null;
    }

    protected  Class getRawClass(Type genericType) {
        if (genericType instanceof ParameterizedType) {
            return getRawClass(ParameterizedType.class.cast(genericType));
        } else if(genericType instanceof Class) {
            return Class.class.cast(genericType);
        }
        return null;
    }


}
