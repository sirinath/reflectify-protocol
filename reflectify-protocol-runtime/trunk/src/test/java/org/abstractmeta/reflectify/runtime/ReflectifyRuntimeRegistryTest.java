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

import org.abstractmeta.reflectify.Reflectify;
import org.abstractmeta.reflectify.ReflectifyRegistry;
import org.testng.Assert;
import org.testng.annotations.Test;

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

}
