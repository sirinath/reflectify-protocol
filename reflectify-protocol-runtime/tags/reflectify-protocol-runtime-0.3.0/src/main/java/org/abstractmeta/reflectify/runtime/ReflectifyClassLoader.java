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
import org.abstractmeta.reflectify.Reflectify;
import org.abstractmeta.reflectify.ReflectifyLoader;
import org.abstractmeta.reflectify.ReflectifyRegistry;
import org.abstractmeta.reflectify.plugin.ReflectifyGenerator;

import java.util.Arrays;
import java.util.List;

/**
 * Represents ReflectifyClassLoader
 *
 * @author Adrian Witas
 */
public class ReflectifyClassLoader implements ReflectifyLoader {

    private final CodeGenerator codeBuilder;

    public ReflectifyClassLoader() {
        this(new CodeGeneratorImpl());
    }

    protected ReflectifyClassLoader(CodeGenerator codeBuilder) {
        this.codeBuilder = codeBuilder;
    }

    @Override
    public Reflectify load(Class type) {
        return load(type, null);
    }

    @Override
    public Reflectify load(Class type, ClassLoader classLoader) {
        Descriptor descriptor = new DescriptorBuilder()
                .setSourceClass(type.getName())
                .setPlugin(ReflectifyGenerator.class.getName()).build();
        SourceCompilerHandler compilerHandler = new SourceCompilerHandler();
        codeBuilder.generate(Arrays.asList(descriptor), compilerHandler, classLoader);
        List<String> generated = compilerHandler.getGeneratedTypes();
        ClassLoader result = compilerHandler.compile();
        try {
            Class generatedClass = result.loadClass(generated.get(0));
            return Reflectify.class.cast(generatedClass.newInstance());

        } catch (Exception e) {
            throw new IllegalStateException("Failed to load Reflectify for " + type, e);
        }

    }
}
