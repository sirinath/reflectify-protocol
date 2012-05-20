package org.abstractmeta.reflectify;


/**
 * Represents a mutator which is used to set a value of a field.
 *
 * @param <I> instance type
 * @param <T> field type
 */
public interface Mutator<I, T> {

    /**
     * Sets value of a field
     * @param instance field owner instance
     * @param value field value
     */
    void set(I instance, T value);

}
