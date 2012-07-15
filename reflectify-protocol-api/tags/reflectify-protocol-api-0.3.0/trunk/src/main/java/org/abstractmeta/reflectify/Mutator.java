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
