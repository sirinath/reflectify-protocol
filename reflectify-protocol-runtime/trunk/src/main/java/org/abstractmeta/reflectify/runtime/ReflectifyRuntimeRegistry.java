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
package org.abstractmeta.reflectify.runtime;

import com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import org.abstractmeta.code.g.CodeGenerator;
import org.abstractmeta.code.g.config.Descriptor;
import org.abstractmeta.code.g.core.CodeGeneratorImpl;
import org.abstractmeta.code.g.core.config.builder.DescriptorBuilder;
import org.abstractmeta.code.g.core.handler.SourceCompilerHandler;
import org.abstractmeta.reflectify.Reflectify;
import org.abstractmeta.reflectify.ReflectifyLoader;
import org.abstractmeta.reflectify.ReflectifyRegistry;
import org.abstractmeta.reflectify.core.ReflectifyRegistryImpl;
import org.abstractmeta.reflectify.plugin.ReflectifyGenerator;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Provides runtime ReflectifyRegistry
 *
 * @author Adrian Witas
 */
public class ReflectifyRuntimeRegistry implements ReflectifyRegistry {

    private final ReflectifyRegistry registry;
    private final ReflectifyLoader reflectifyClassLoader;


    public ReflectifyRuntimeRegistry() {
        this(new ReflectifyRegistryImpl());
    }

    public ReflectifyRuntimeRegistry(ReflectifyRegistry registry) {
        this(registry, new ReflectifyClassLoader());
    }

    protected ReflectifyRuntimeRegistry(ReflectifyRegistry registry, ReflectifyLoader reflectifyClassLoader) {
        this.registry = registry;
        this.reflectifyClassLoader = reflectifyClassLoader;
    }

    @Override
    public void register(Reflectify reflectifyProtocol) {
        registry.register(reflectifyProtocol);
    }

    @Override
    public void registerAll(Collection<Reflectify> reflectify) {
        registry.registerAll(reflectify);
    }

    @Override
    public boolean isRegistered(Class type) {
        return registry.isRegistered(type);
    }

    @Override
    public void unregister(Class type) {
        registry.unregister(type);
    }

    @Override
    public void unregisterAll() {
        registry.unregisterAll();
    }

    @Override
    public <T> Reflectify<T> get(Class<T> type) {
        if (!isRegistered(type)) {
            Reflectify<T> protocol = load(type);
            registry.register(protocol);
        }
        return registry.get(type);
    }


    public Map<Class, Reflectify> getRegistry() {
        return registry.getRegistry();
    }

    public <T> Reflectify<T> load(Class<T> type) {
        return reflectifyClassLoader.load(type);
    }


}
