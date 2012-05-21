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

import org.abstractmeta.reflectify.ReflectifyProtocol;
import org.abstractmeta.reflectify.ReflectifyRegistry;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents ReflectifyProxyRegistry
 *
 * @author Adrian Witas
 */
public class ReflectifyProxyRegistry implements ReflectifyRegistry {


    private final Collection<ReflectifyRegistry> reflectifyRegistries;

    public ReflectifyProxyRegistry(Collection<ReflectifyRegistry> reflectifyRegistries) {
        this.reflectifyRegistries = reflectifyRegistries;
    }

    @Override
    public void register(ReflectifyProtocol reflectifyToolbox) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void registerAll(Collection<ReflectifyProtocol> reflectifyToolboxes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRegistered(Class type) {
        for (ReflectifyRegistry reflectifyRegistry : reflectifyRegistries) {
            if (reflectifyRegistry.isRegistered(type)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void unregister(Class type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void unregisterAll() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> ReflectifyProtocol<T> get(Class<T> type) {
        for (ReflectifyRegistry reflectifyRegistry : reflectifyRegistries) {
            ReflectifyProtocol<T> result = reflectifyRegistry.get(type);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    @Override
    public Collection<ReflectifyProtocol> getReflectifyProtocols() {
        Collection<ReflectifyProtocol> result = new ArrayList<ReflectifyProtocol>();
        for (ReflectifyRegistry registry : reflectifyRegistries) {
            result.addAll(registry.getReflectifyProtocols());
        }
        return result;
    }


}
