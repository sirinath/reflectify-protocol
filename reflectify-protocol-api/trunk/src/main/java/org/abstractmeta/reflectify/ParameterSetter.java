package org.abstractmeta.reflectify;

/**
* Represents a method/constructor parameter setter.
 * It sets value of constructor or method parameter.
*
* @author Adrian Witas
*/
public interface ParameterSetter<T> {

    void set(T value);

}
