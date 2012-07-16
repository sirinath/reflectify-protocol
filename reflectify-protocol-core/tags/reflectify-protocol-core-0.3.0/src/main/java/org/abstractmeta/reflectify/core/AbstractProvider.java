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
