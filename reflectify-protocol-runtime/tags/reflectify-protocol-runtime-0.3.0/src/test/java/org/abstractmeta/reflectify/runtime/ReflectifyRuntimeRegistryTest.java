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

import org.abstractmeta.reflectify.MethodInvoker;
import org.abstractmeta.reflectify.Reflectify;
import org.abstractmeta.reflectify.ReflectifyRegistry;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Test
public class ReflectifyRuntimeRegistryTest {

    public void testReflectifyRuntimeRegistry() {
        ReflectifyRegistry registry = new ReflectifyRuntimeRegistry();
        Assert.assertFalse(registry.isRegistered(Dept.class));
        Reflectify<Dept> deptReflectify = registry.get(Dept.class);
        Assert.assertTrue(registry.isRegistered(Dept.class));
        Dept dept = deptReflectify.getProvider().get();
        deptReflectify.getMutator("name").set(dept, "name 1");
        Assert.assertEquals(dept.getName(), "name 1");
        dept.setId(12);
        Assert.assertEquals(deptReflectify.getAccessor("id").get(dept), 12);
    }


    public void testReflectifyRuntimeRegistryInnerClass() {
        ReflectifyRegistry registry = new ReflectifyRuntimeRegistry();
        Assert.assertFalse(registry.isRegistered(Employee.class));
        Reflectify<Employee> empReflectify = registry.get(Employee.class);
        Assert.assertTrue(registry.isRegistered(Employee.class));
        Employee emp = empReflectify.getProvider().get();
        empReflectify.getMutator("name").set(emp, "name 1");
        Assert.assertEquals(emp.getName(), "name 1");
        emp.setId(12);
        Assert.assertEquals(empReflectify.getAccessor("id").get(emp), 12);
    }

    public void testReflectifyWithList() {
        ReflectifyRegistry registry = new ReflectifyRuntimeRegistry();
        Assert.assertFalse(registry.isRegistered(List.class));
        Reflectify<List> empReflectify = registry.get(List.class);
        Assert.assertTrue(registry.isRegistered(List.class));
        List<Integer> list = new ArrayList<Integer>();
        MethodInvoker<List, Boolean> addMethod = empReflectify.getMethodInvoker(boolean.class, "add", Integer.class);
        Assert.assertNotNull(addMethod);
        addMethod.getParameterSetter(0).set(100);
        addMethod.invoke(list);
        Assert.assertEquals(list.size(), 1);
        Assert.assertEquals(list.get(0), new Integer(100));
    }

    public void testReflectifyWithMap() {
        ReflectifyRegistry registry = new ReflectifyRuntimeRegistry();
        Assert.assertFalse(registry.isRegistered(Map.class));
        Reflectify<Map> mapReflectify = registry.get(Map.class);
        Assert.assertTrue(registry.isRegistered(Map.class));
        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        MethodInvoker<Map, Boolean> putMethod = mapReflectify.getMethodInvoker(boolean.class, "put", Integer.class, Integer.class);
        Assert.assertNotNull(putMethod);
        putMethod.getParameterSetter(0).set(1);
        putMethod.getParameterSetter(1).set(2);
        putMethod.invoke(map);
        Assert.assertEquals(map.size(), 1);
        Assert.assertEquals(map.get(1), new Integer(2));
    }


    @Test(expectedExceptions = RuntimeException.class)
    public void testReflectifyWithExceptionHandling() {
        ReflectifyRegistry registry = new ReflectifyRuntimeRegistry();
        Assert.assertFalse(registry.isRegistered(Bar.class));
        Reflectify<Bar> barReflectify = registry.get(Bar.class);
       barReflectify.getProvider(boolean.class).get();
    }


    @Test(expectedExceptions = RuntimeException.class)
    public void testReflectifyWithMethodExceptionHandling() {
        ReflectifyRegistry registry = new ReflectifyRuntimeRegistry();
        Assert.assertFalse(registry.isRegistered(Bar.class));
        Reflectify<Bar> barReflectify = registry.get(Bar.class);
        MethodInvoker<Bar, Void> fooMethod = barReflectify.getMethodInvoker(void.class, "foo");
        Bar b = new Bar();
        Assert.assertNotNull(fooMethod);
        fooMethod.invoke(b);
    }


    public void testDuplicateAccessorFix() {
        ReflectifyRegistry registry = new ReflectifyRuntimeRegistry();
        Assert.assertFalse(registry.isRegistered(BooleanFix.class));
        Reflectify<BooleanFix> reflectify = registry.get(BooleanFix.class);
        Assert.assertTrue(registry.isRegistered(BooleanFix.class));
        BooleanFix instance = reflectify.getProvider().get();
        reflectify.getMutator("b").set(instance, true);
        Assert.assertEquals(instance.isB(), true);
        Assert.assertEquals(reflectify.getAccessor("b").get(instance), true);
    }

    public static class Bar {

        
        public Bar() {
            
        }
        public Bar(boolean exception) throws Exception {
               throw new Exception("test");
        }

        public void foo() throws IOException {
             throw new IOException("test") ;
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

    public static enum Policy {
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
