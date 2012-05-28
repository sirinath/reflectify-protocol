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

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;

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
            for(Method method: clazz.getMethods()) {
                if(method.getName().equals(methodName)) {
                    System.out.println(Arrays.asList(method.getParameterTypes()));
                    
                }
            }
            
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
