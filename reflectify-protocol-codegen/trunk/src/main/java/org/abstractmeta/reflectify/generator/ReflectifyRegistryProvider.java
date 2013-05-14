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
package org.abstractmeta.reflectify.generator;

import org.abstractmeta.code.g.code.CompiledJavaType;
import org.abstractmeta.code.g.code.CompiledJavaTypeRegistry;
import org.abstractmeta.code.g.config.UnitDescriptor;
import org.abstractmeta.code.g.core.generator.CodeUnitGeneratorImpl;
import org.abstractmeta.code.g.generator.CodeUnitGenerator;
import org.abstractmeta.code.g.generator.GeneratedCode;
import org.abstractmeta.reflectify.Reflectify;
import org.abstractmeta.reflectify.ReflectifyRegistry;
import org.abstractmeta.reflectify.core.ReflectifyRegistryImpl;
import org.abstractmeta.reflectify.generator.util.UnitDescriptorUtil;

import javax.inject.Provider;
import java.util.Collection;

/**
 * ReflectifyRegistry
 */
public class ReflectifyRegistryProvider implements Provider<ReflectifyRegistry> {

    private final CompiledJavaType compiledJavaType;

    public ReflectifyRegistryProvider(Class... classes) {
        compiledJavaType = compileReflectify(classes);
    }

    @Override
    public ReflectifyRegistry get() {
        try {
            ReflectifyRegistry result = new ReflectifyRegistryImpl();
            @SuppressWarnings("unchecked")
            Provider<Collection<Reflectify>> collectionProvider = Provider.class.cast(compiledJavaType.getCompiledType().newInstance());
            result.registerAll(collectionProvider.get());
            return result;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create reflectify provider", e);
        }
    }

    protected CompiledJavaType compileReflectify(Class... classes) {
        UnitDescriptor unitDescriptor = UnitDescriptorUtil.getUnitDescriptor(classes);
        CodeUnitGenerator unitGenerator = new CodeUnitGeneratorImpl();
        GeneratedCode generatedCode = unitGenerator.generate(unitDescriptor);
        CompiledJavaTypeRegistry registry = generatedCode.getRegistry();
        return getReflectifyProvider(registry);

    }

    protected CompiledJavaType getReflectifyProvider(CompiledJavaTypeRegistry registry) {
        for (CompiledJavaType candidate : registry.get()) {
            if (candidate.getCompiledType().getSimpleName().equals(ReflectifyProviderGenerator.REFLECTIFY_PROVIDER)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Failed to lookup ReflecifyProvider");
    }
}
