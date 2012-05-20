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
