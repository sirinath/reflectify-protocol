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

/**
 * Represents MethodInvoker, which invoke instance method.
 *
 * @author Adrian Witas
 */

public interface MethodInvoker<I, T> {

    /**
     * Returns a method argument setter
     * @param argumentType argument Type
     * @param argumentIndex method argument index starting from 0
     * @param <T> argument type
     * @return argument setter
     *
     */
    <T> ParameterSetter<T> getParameterSetter(Class<T> argumentType, int argumentIndex);

    /**
     * Returns a method argument setter of a given argument index
     * @param argumentIndex method argument index starting from 0
     * @return argument setter
     */
    ParameterSetter<Object> getParameterSetter(int argumentIndex);

    /**
     * Executes a method of a given instance
     * @param instance method owner
     * @return method result
     */
    T invoke(I instance);

    /**
     * Returns methods argument types.
     * @return method argument types.
     */
    Type [] getGenericParameterTypes();

    /**
     * Returns parameters type.
     * @return
     */
    Class [] getParameterTypes();

    /**
     * Returns method result type.
     * @return method result type.
     */
    Type getResultGenericType();

    /**
     * Return result type.
     * @return
     */
    Class getResultType();

}
