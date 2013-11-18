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
package org.abstractmeta.reflectify.generator.util;

import org.abstractmeta.code.g.config.Descriptor;
import org.abstractmeta.code.g.config.UnitDescriptor;
import org.abstractmeta.code.g.core.config.DescriptorImpl;
import org.abstractmeta.code.g.core.config.SourceMatcherImpl;
import org.abstractmeta.code.g.core.config.UnitDescriptorImpl;
import org.abstractmeta.reflectify.generator.ReflectifyGenerator;
import org.abstractmeta.reflectify.generator.ReflectifyProviderGenerator;
import org.abstractmeta.toolbox.compilation.compiler.util.ClassPathUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public class UnitDescriptorUtil {


    public static UnitDescriptor getUnitDescriptor(Class... classes) {
        UnitDescriptorImpl result = new UnitDescriptorImpl();
        DescriptorImpl descriptor = new DescriptorImpl();
        SourceMatcherImpl sourceMatcher = new SourceMatcherImpl();
        List<String> classesCollection = new ArrayList<String>();
        for (Class clazz : classes) classesCollection.add(clazz.getName());
        sourceMatcher.setClassNames(classesCollection);
        descriptor.setSourceMatcher(sourceMatcher);
        descriptor.setGeneratorClass(ReflectifyGenerator.class.getName());
        result.setDescriptors(Arrays.<Descriptor>asList(descriptor));
        result.setClassPathEntries(new ArrayList<String>(ClassPathUtil.getClassPathEntries()));
        File tempFile;
        try {
            tempFile = File.createTempFile("reflectify", "temp");
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create target directory");
        }
        File targetSourceDirectory = new File(tempFile.getParentFile(), tempFile.getName() + "-source");
        targetSourceDirectory.mkdirs();
        result.setTargetSourceDirectory(targetSourceDirectory.getAbsolutePath());

        File compilationDirectory = new File(tempFile.getParentFile(), tempFile.getName() + "-compilation");
        compilationDirectory.mkdirs();
        result.setTargetCompilationDirectory(compilationDirectory.getAbsolutePath());


        return result;
    }


}
