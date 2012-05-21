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

import org.abstractmeta.code.g.CodeGenerator;
import org.abstractmeta.code.g.config.Descriptor;
import org.abstractmeta.code.g.core.CodeGeneratorImpl;
import org.abstractmeta.code.g.core.config.builder.DescriptorBuilder;
import org.abstractmeta.code.g.core.handler.SourceCompilerHandler;
import org.abstractmeta.reflectify.ReflectifyProtocol;
import org.abstractmeta.reflectify.ReflectifyRegistry;
import org.abstractmeta.reflectify.core.ReflectifyRegistryImpl;
import org.abstractmeta.reflectify.plugin.ReflectifyGenerator;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Provides runtime ReflectifyRegistry
 *
 * @author Adrian Witas
 */
public class ReflectifyRuntimeRegistry implements ReflectifyRegistry {

    private final ReflectifyRegistry registry;
    private final CodeGenerator codeBuilder;


    public ReflectifyRuntimeRegistry() {
        this(new ReflectifyRegistryImpl());
    }

    public ReflectifyRuntimeRegistry(ReflectifyRegistry registry) {
        this(registry, new CodeGeneratorImpl());
    }

    protected ReflectifyRuntimeRegistry(ReflectifyRegistry registry, CodeGenerator codeBuilder) {
        this.registry = registry;
        this.codeBuilder = codeBuilder;
    }

    @Override
    public void register(ReflectifyProtocol reflectifyProtocol) {
        registry.register(reflectifyProtocol);
    }

    @Override
    public void registerAll(Collection<ReflectifyProtocol> reflectifyProtocols) {
        registry.registerAll(reflectifyProtocols);
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
    public <T> ReflectifyProtocol<T> get(Class<T> type) {
        if (!isRegistered(type)) {
            ReflectifyProtocol<T> protocol = load(type);
            registry.register(protocol);
        }
        return registry.get(type);
    }

    @Override
    public Collection<ReflectifyProtocol> getReflectifyProtocols() {
        return registry.getReflectifyProtocols();
    }

    public ReflectifyRegistry getRegistry() {
        return registry;
    }

    public <T> ReflectifyProtocol<T> load(Class<T> type) {
        Descriptor descriptor = new DescriptorBuilder()
                .setSourceClass(type.getName())
                .setPlugin(ReflectifyGenerator.class.getName()).build();
        SourceCompilerHandler compilerHandler = new SourceCompilerHandler();
        codeBuilder.generate(Arrays.asList(descriptor), compilerHandler);
        List<String> generated = compilerHandler.getGeneratedTypes();
        ClassLoader classLoader = compilerHandler.compile();
        try {
            Class generatedClass = classLoader.loadClass(generated.get(0));
            @SuppressWarnings("unchecked")
            ReflectifyProtocol<T> result = (ReflectifyProtocol<T>) generatedClass.newInstance();
            return result;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load ReflectifyProtocol for " + type);
        }
    }

}
