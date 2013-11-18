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
import org.abstractmeta.code.g.code.JavaTypeRegistry;
import org.abstractmeta.code.g.config.UnitDescriptor;
import org.abstractmeta.code.g.core.generator.CodeUnitGeneratorImpl;
import org.abstractmeta.code.g.generator.CodeUnitGenerator;
import org.abstractmeta.code.g.generator.GeneratedCode;
import org.abstractmeta.reflectify.MethodInvoker;
import org.abstractmeta.reflectify.Reflectify;
import org.abstractmeta.reflectify.ReflectifyRegistry;
import org.abstractmeta.reflectify.core.ReflectifyRegistryImpl;
import org.abstractmeta.reflectify.generator.util.UnitDescriptorUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.inject.Provider;
import java.io.IOException;
import java.util.*;


@Test
public class ReflectifyGeneratorTest {


    public void testReflectifyGenerator() throws Exception {
        ReflectifyRegistry reflectifyRegistry = new ReflectifyRegistryProvider(Employee.class).get();
        Reflectify<Employee> employeeReflectify = reflectifyRegistry.get(Employee.class);
        Employee employee = employeeReflectify.getProvider().get();
        Assert.assertNotNull(employee);
    }


    public void testReflectifyWithListGenerator() {
        ReflectifyRegistry reflectifyRegistry = new ReflectifyRegistryProvider(List.class).get();
        Reflectify<List> listReflectify = reflectifyRegistry.get(List.class);
        List<String> list = new ArrayList<String>();
        MethodInvoker<List, Void> methodInvoker = listReflectify.getMethodInvoker(void.class, "add", Object.class);
        methodInvoker.getParameterSetter(Object.class, 0).set(10);
        for (int i = 0; i < 10; i++) {
            methodInvoker.invoke(list);
        }
        Assert.assertEquals(list.size(), 10);

    }

    public void testReflectifyWithMapGenerator() {

        ReflectifyRegistry reflectifyRegistry = new ReflectifyRegistryProvider(Map.class).get();
        Reflectify<Map> listReflectify = reflectifyRegistry.get(Map.class);
        Map<String, String> map = new HashMap<String, String>();
        MethodInvoker<Map, Void> methodInvoker = listReflectify.getMethodInvoker(void.class, "put", Object.class, Object.class);
        for (int i = 0; i < 10; i++) {
            methodInvoker.getParameterSetter(Object.class, 0).set("key " + i);
            methodInvoker.getParameterSetter(Object.class, 1).set("value " + i);
            methodInvoker.invoke(map);
        }
        Assert.assertEquals(map.size(), 10);
    }


    public void testReflectifyWithExceptionSupportGenerator() {
        ReflectifyRegistry reflectifyRegistry = new ReflectifyRegistryProvider(Bar.class).get();
        Reflectify<Bar> barReflectify = reflectifyRegistry.get(Bar.class);
        Bar bar = barReflectify.getProvider().get();
        Assert.assertNotNull(bar);
    }


    public void testBooleanFix() {
        ReflectifyRegistry reflectifyRegistry = new ReflectifyRegistryProvider(BooleanFix.class).get();
        Reflectify<BooleanFix> booleanReflectify = reflectifyRegistry.get(BooleanFix.class);
        BooleanFix booleanFix = booleanReflectify.getProvider().get();
        Assert.assertNotNull(booleanReflectify);
        Assert.assertFalse(booleanReflectify.getAccessor(boolean.class, "b").get(booleanFix));
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
        private Dept[] depds;

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

    public enum Policy {
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
