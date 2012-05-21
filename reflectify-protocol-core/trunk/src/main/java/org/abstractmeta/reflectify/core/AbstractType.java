/**
 * Copyright 2011 Adrian Witas
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
