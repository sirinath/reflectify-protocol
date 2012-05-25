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


import java.util.Collection;

/**
 * Represents reflectify protocol registry.
 *
 * @author Adrian Witas
 */

public interface ReflectifyRegistry {

    void register(Reflectify reflectifyProtocol);

    void registerAll(Collection<Reflectify> reflectifyProtocols);

    boolean isRegistered(Class type);

    void unregister(Class type);

     void unregisterAll();

    <T> Reflectify<T> get(Class<T> type);

    Collection<Reflectify> getReflectifyProtocols();

}
