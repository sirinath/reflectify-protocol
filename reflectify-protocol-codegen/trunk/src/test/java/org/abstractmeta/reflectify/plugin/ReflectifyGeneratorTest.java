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
package org.abstractmeta.reflectify.plugin;

import com.sun.tools.apt.util.Bark;
import org.abstractmeta.code.g.code.JavaType;
import org.abstractmeta.code.g.code.JavaTypeImporter;
import org.abstractmeta.code.g.code.JavaTypeRegistry;
import org.abstractmeta.code.g.config.Descriptor;
import org.abstractmeta.code.g.core.code.JavaTypeImporterImpl;
import org.abstractmeta.code.g.core.code.JavaTypeRegistryImpl;
import org.abstractmeta.code.g.core.config.builder.DescriptorBuilder;
import org.abstractmeta.code.g.core.provider.ClassTypeProvider;
import org.abstractmeta.code.g.core.renderer.TypeRenderer;
import org.abstractmeta.code.g.renderer.JavaTypeRenderer;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


@Test
public class ReflectifyGeneratorTest {


    public void testReflectifyGenerator() {
        ReflectifyGenerator generator = new ReflectifyGenerator();
        JavaTypeRegistry registry = new JavaTypeRegistryImpl();
        JavaType javaType = new ClassTypeProvider(Employee.class).get();
        registry.register(javaType);
        Descriptor descriptor = new DescriptorBuilder()
                .setSourceClass(Employee.class.getName())
                .setSourcePackage(Employee.class.getPackage().getName())
                .setPlugin(ReflectifyGenerator.class.getName())
                .build();
        List<String> build = generator.generate(Arrays.asList(javaType.getName()), registry, descriptor);
            String typeName = build.get(0);
            JavaType builtType = registry.get(typeName);
            JavaTypeRenderer renderer = new TypeRenderer();
            JavaTypeImporter importer = new JavaTypeImporterImpl(Employee.class.getPackage().getName() + ".reflectify");
            String code = renderer.render(builtType, importer, 0);
            Assert.assertNotNull(code);
    }


    public void testReflectifyWithListGenerator() {
        ReflectifyGenerator generator = new ReflectifyGenerator();
        JavaTypeRegistry registry = new JavaTypeRegistryImpl();
        JavaType javaType = new ClassTypeProvider(List.class).get();
        registry.register(javaType);
        Descriptor descriptor = new DescriptorBuilder()
                .setSourceClass(List.class.getName())
                .setSourcePackage(List.class.getPackage().getName())
                .setPlugin(ReflectifyGenerator.class.getName())
                .build();
        List<String> build = generator.generate(Arrays.asList(javaType.getName()), registry, descriptor);
        String typeName = build.get(0);
        JavaType builtType = registry.get(typeName);
        JavaTypeRenderer renderer = new TypeRenderer();
        JavaTypeImporter importer = new JavaTypeImporterImpl(Employee.class.getPackage().getName() + ".reflectify");
        String code = renderer.render(builtType, importer, 0);
        Assert.assertNotNull(code,  "");
    }



    public void testReflectifyWithMapGenerator() {
        ReflectifyGenerator generator = new ReflectifyGenerator();
        JavaTypeRegistry registry = new JavaTypeRegistryImpl();
        JavaType javaType = new ClassTypeProvider(Map.class).get();
        registry.register(javaType);
        Descriptor descriptor = new DescriptorBuilder()
                .setSourceClass(Map.class.getName())
                .setSourcePackage(Map.class.getPackage().getName())
                .setPlugin(ReflectifyGenerator.class.getName())
                .build();
        List<String> build = generator.generate(Arrays.asList(javaType.getName()), registry, descriptor);
        String typeName = build.get(0);
        JavaType builtType = registry.get(typeName);
        JavaTypeRenderer renderer = new TypeRenderer();
        JavaTypeImporter importer = new JavaTypeImporterImpl(Employee.class.getPackage().getName() + ".reflectify");
        String code = renderer.render(builtType, importer, 0);
        Assert.assertNotNull(code,  "");
    }


    public void testReflectifyWithExceptionSupportGenerator() {
        ReflectifyGenerator generator = new ReflectifyGenerator();
        JavaTypeRegistry registry = new JavaTypeRegistryImpl();
        JavaType javaType = new ClassTypeProvider(Bar.class).get();
        registry.register(javaType);
        Descriptor descriptor = new DescriptorBuilder()
                .setSourceClass(Bar.class.getName())
                .setSourcePackage(Bar.class.getPackage().getName())
                .setPlugin(ReflectifyGenerator.class.getName())
                .build();
        List<String> build = generator.generate(Arrays.asList(javaType.getName()), registry, descriptor);
        String typeName = build.get(0);
        JavaType builtType = registry.get(typeName);
        JavaTypeRenderer renderer = new TypeRenderer();
        JavaTypeImporter importer = new JavaTypeImporterImpl(Bar.class.getPackage().getName() + ".reflectify");
        String code = renderer.render(builtType, importer, 0);
        Assert.assertNotNull(code,  "");
    }



    public void testBooleanFix() {
        ReflectifyGenerator generator = new ReflectifyGenerator();
        JavaTypeRegistry registry = new JavaTypeRegistryImpl();
        JavaType javaType = new ClassTypeProvider(BooleanFix.class).get();
        registry.register(javaType);
        Descriptor descriptor = new DescriptorBuilder()
                .setSourceClass(BooleanFix.class.getName())
                .setSourcePackage(BooleanFix.class.getPackage().getName())
                .setPlugin(ReflectifyGenerator.class.getName())
                .build();
        List<String> build = generator.generate(Arrays.asList(javaType.getName()), registry, descriptor);
        String typeName = build.get(0);
        JavaType builtType = registry.get(typeName);
        JavaTypeRenderer renderer = new TypeRenderer();
        JavaTypeImporter importer = new JavaTypeImporterImpl(BooleanFix.class.getPackage().getName() + ".reflectify");
        String code = renderer.render(builtType, importer, 0);
        Assert.assertNotNull(code);
    //    Assert.assertEquals(code, "");
    }

    public static class Bar {
       
        public Bar() throws Exception {

        }
        
        public void foo() throws IOException {
            
        }
        
    }

    public static class Dept {
        private int id;
        private String name;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class Employee {
        private int id;
        private String name;
        private Policy policy;
        private Dept [] depds;

        public Dept[] getDepds() {
            return depds;
        }

        public void setDepds(Dept[] depds) {
            this.depds = depds;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Policy getPolicy() {
            return policy;
        }

        public void setPolicy(Policy policy) {
            this.policy = policy;
        }
    }

    public  enum Policy {
        BASIC, ADVANCE
    }

    
    public static class BooleanFix {
        private String a;
        private boolean b;

        public String getA() {
            return a;
        }

        public void setA(String a) {
            this.a = a;
        }

        public boolean isB() {
            return b;
        }

        public void setB(boolean b) {
            this.b = b;
        }
    }
}
