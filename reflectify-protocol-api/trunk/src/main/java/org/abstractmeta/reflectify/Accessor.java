package org.abstractmeta.reflectify;




/**
 * Represents an ancestor which is is used to return the value of a field.
 * @author Adrian Witas
 *
 * @param <I> instance type
 * @param <T> field type
 */
public interface Accessor<I, T> {


    /**
     * Returns value of a field.
     * @param instance owner instance of a field
     * @return field value
     */
    T get(I instance);
}
