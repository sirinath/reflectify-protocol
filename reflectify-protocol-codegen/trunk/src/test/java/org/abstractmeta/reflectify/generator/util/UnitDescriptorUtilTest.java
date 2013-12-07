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

import junit.framework.Assert;
import org.abstractmeta.code.g.code.CompiledJavaType;
import org.abstractmeta.code.g.code.CompiledJavaTypeRegistry;
import org.abstractmeta.code.g.config.UnitDescriptor;
import org.abstractmeta.code.g.core.generator.CodeUnitGeneratorImpl;
import org.abstractmeta.code.g.generator.CodeUnitGenerator;
import org.abstractmeta.code.g.generator.GeneratedCode;
import org.abstractmeta.reflectify.Reflectify;
import org.abstractmeta.reflectify.ReflectifyRegistry;
import org.abstractmeta.reflectify.core.ReflectifyRegistryImpl;
import org.abstractmeta.reflectify.generator.helper.Employee;
import org.testng.annotations.Test;

import javax.inject.Provider;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;

/**
 * Represents UnitDescriptorUtilTest
 * <p>
 * </p>
 *
 * @author Adrian Witas
 */
@Test
public class UnitDescriptorUtilTest {

    public void testDescriptor() throws Exception {
        File sourceDirectory = new File("src/test/java");
        File targetSourceDirectory = new File("target/generated-test-sources/code-g");
        UnitDescriptor unitDescriptor = UnitDescriptorUtil.getReflectifyUnitDescriptorByPackages(sourceDirectory, targetSourceDirectory, Arrays.asList(Employee.class.getPackage().getName()));
        CodeUnitGenerator unitGenerator = new CodeUnitGeneratorImpl();
        GeneratedCode generatedCode = unitGenerator.generate(unitDescriptor);
        CompiledJavaTypeRegistry registry = generatedCode.getRegistry();

        CompiledJavaType registryProvideType = registry.get("org.abstractmeta.reflectify.generator.helper.reflectify.ReflectifyProvider");
        Provider<Collection<Reflectify>> provider = (Provider)registryProvideType.getCompiledType().newInstance();
        ReflectifyRegistry reflectifyRegistry = new ReflectifyRegistryImpl();
        reflectifyRegistry.registerAll(provider.get());
        Reflectify<Employee> employeeReflectify = reflectifyRegistry.get(Employee.class);
        Employee  employee = employeeReflectify.getProvider().get();
        Assert.assertNotNull(employee);


    }

}
