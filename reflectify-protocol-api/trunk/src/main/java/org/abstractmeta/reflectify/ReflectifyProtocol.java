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
package org.abstractmeta.reflectify;


import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

/**
 * Represents ReflectifyProtocol.
 * It provides api for reflective data system.
 *
 * @author Adrian Witas
 */

public interface ReflectifyProtocol<I> {

    Class<I> getType();

    Provider<I> getProvider(Class... argumentTypes);
    
    List<Provider<I>> getProviders();

    <T> MethodInvoker<I, T> getMethodInvoker(Class<T> methodResultType, String methodName, Class... argumentTypes);

    MethodInvoker<I, Object> getMethodInvoker(String methodName, Class... argumentTypes);

    List<MethodInvoker<I, Object>> getMethodInvokers(String methodName);

    <T> Accessor<I, T> getAccessor(Class<T> fieldType, String fieldName);

    Accessor<I, Object> getAccessor(String fieldName);

    <T> Mutator<I, T> getMutator(Class<T> fieldType, String fieldName);

    Mutator<I, Object> getMutator(String fieldName);
    
    List<String> getFieldNames();
    
    Class getFieldType(String fieldName);
    
    List<String> getMethodNames();
    
    interface Provider<I> extends javax.inject.Provider<I> {
        /**
         * Returns a method argument setter
         * @param argumentType argument Type
         * @param argumentIndex method argument index starting from 0
         */
        <T> ParameterSetter<T> getParameterSetter(Class<T> argumentType, int argumentIndex);

        /**
         * Returns a method argument setter
         * @param argumentIndex method argument index starting from 0
         */
        ParameterSetter<Object> getParameterSetter(int argumentIndex);


        /**
         * Returns provider parameters' types
         *
         * @return
         */
        Type[] getGenericParameterTypes();

        /**
         * Returns parameter types.
         * @return
         */
        Class [] getParameterTypes();
        
    }

}
