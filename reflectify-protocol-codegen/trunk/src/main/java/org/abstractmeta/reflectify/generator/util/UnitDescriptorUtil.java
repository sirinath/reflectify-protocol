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
import org.abstractmeta.code.g.config.SourceMatcher;
import org.abstractmeta.code.g.config.UnitDescriptor;
import org.abstractmeta.code.g.core.config.DescriptorImpl;
import org.abstractmeta.code.g.core.config.UnitDescriptorImpl;
import org.abstractmeta.reflectify.generator.ReflectifyGenerator;
import org.abstractmeta.reflectify.generator.ReflectifyProviderGenerator;

import java.io.File;
import java.util.Collection;

import static org.abstractmeta.code.g.core.util.UnitDescriptorUtil.*;
import static org.abstractmeta.code.g.core.util.UnitDescriptorUtil.getMatcherByClassNames;

/**
 *
 */
public class UnitDescriptorUtil {


    public static UnitDescriptor getReflectifyUnitDescriptor(File targetSourceDirectory , Class... classes) {
        File tempDirectory = getTempDirectory();
        File sourceDirectory = getTempDirectory(tempDirectory, "source");
        File compilationDirectory = getTempDirectory(tempDirectory, "-compilation");
        UnitDescriptorImpl result = new UnitDescriptorImpl();
        SourceMatcher sourceMatcher = getMatcherByClassNames(sourceDirectory, classes);
        updateUnitDescriptor(result, sourceDirectory, targetSourceDirectory, null, ReflectifyGenerator.class, sourceMatcher);
        result.setTargetCompilationDirectory(compilationDirectory.getAbsolutePath());
        return result;
    }


    public static UnitDescriptor getReflectifyUnitDescriptorByPackages(File sourceDirectory, File targetSourceDirectory, Collection<String> packageNames) {
        File tempDirectory = getTempDirectory();
        File compilationDirectory = getTempDirectory(tempDirectory, "-compilation");
        UnitDescriptorImpl result = new UnitDescriptorImpl();
        SourceMatcher sourceMatcher = getSourceMatcherByPackageNames(sourceDirectory, packageNames);
        updateUnitDescriptor(result, sourceDirectory, targetSourceDirectory, null, ReflectifyGenerator.class, sourceMatcher);
        result.setTargetCompilationDirectory(compilationDirectory.getAbsolutePath());
        Descriptor providerDescriptor = getProviderGeneratorDescriptor();
        result.setPostDescriptor(providerDescriptor);
        return result;
    }

    public static Descriptor getProviderGeneratorDescriptor() {
        DescriptorImpl descriptor = new DescriptorImpl();
        descriptor.setGeneratorClass(ReflectifyProviderGenerator.class.getName());
        return descriptor;
    }


    public static UnitDescriptor getReflectifyUnitDescriptorByClassNames(File sourceDirectory, File targetSourceDirectory, Collection<String> classNames, boolean generateProvider) {
        File tempDirectory = getTempDirectory();
        File compilationDirectory = getTempDirectory(tempDirectory, "-compilation");
        UnitDescriptorImpl result = new UnitDescriptorImpl();
        SourceMatcher sourceMatcher = getSourceMatcherByClassNames(sourceDirectory, classNames);
        updateUnitDescriptor(result, sourceDirectory, targetSourceDirectory, null, ReflectifyGenerator.class, sourceMatcher);
        result.setTargetCompilationDirectory(compilationDirectory.getAbsolutePath());
        Descriptor providerDescriptor = getProviderGeneratorDescriptor();
        result.setPostDescriptor(providerDescriptor);
        return result;
    }


}