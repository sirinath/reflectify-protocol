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
import org.abstractmeta.code.g.core.plugin.BuilderGeneratorPlugin;
import org.abstractmeta.code.g.core.plugin.ClassGeneratorPlugin;
import org.abstractmeta.code.g.core.util.JavaTypeUtil;
import org.abstractmeta.reflectify.Reflectify;
import org.abstractmeta.reflectify.ReflectifyLoader;
import org.abstractmeta.reflectify.ReflectifyRegistry;
import org.abstractmeta.reflectify.plugin.ReflectifyGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Load reflectify for a type's builder.
 *
 * @author Adrian Witas
 */
public class ReflectifyClassBuilderLoader implements ReflectifyLoader {

    private final CodeGenerator codeBuilder;
    private final SourceCompilerHandler compilerHandler;

    public ReflectifyClassBuilderLoader() {
        this(new CodeGeneratorImpl(), new SourceCompilerHandler());
    }

    protected ReflectifyClassBuilderLoader(CodeGenerator codeBuilder, SourceCompilerHandler compilerHandler) {
        this.codeBuilder = codeBuilder;
        this.compilerHandler = compilerHandler;
    }


    protected String getTypeName(Class type) {
        String result = JavaTypeUtil.getSimpleClassName(type.getName(), true);
        result = result.replace(".", "");
        return result;
    }


    @Override
    public Reflectify load(Class type) {
        return load(type, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Reflectify load(Class type, ClassLoader classLoader) {
        DescriptorBuilder builderDescriptor = new DescriptorBuilder();
        builderDescriptor.setPlugin(BuilderGeneratorPlugin.class.getName());
        List<String> pluginNames = new ArrayList<String>();
        if (type.isInterface()) {
            pluginNames.add(ClassGeneratorPlugin.class.getName());
        }
        pluginNames.addAll(Arrays.asList(BuilderGeneratorPlugin.class.getName(), ReflectifyGenerator.class.getName()));

        for (String pluginName : pluginNames) {
            Descriptor descriptor = new DescriptorBuilder()
                    .setSourceClass(type.getName())
                    .setPlugin(pluginName).build();
            codeBuilder.generate(Arrays.asList(descriptor), compilerHandler, classLoader);
            List<String> generated = compilerHandler.getGeneratedTypes();
            classLoader = compilerHandler.compile(classLoader);
            try {
                type = classLoader.loadClass(generated.get(0));
                generated.clear();
            } catch (Exception e) {
                throw new IllegalStateException("Failed to load class " + generated.get(0));
            }
        }


        try {
            return (Reflectify) type.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to instantiate class " + type, e);
        }
    }


}