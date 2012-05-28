package org.abstractmeta.reflectify.plugin;

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

import java.util.Arrays;
import java.util.List;


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

}