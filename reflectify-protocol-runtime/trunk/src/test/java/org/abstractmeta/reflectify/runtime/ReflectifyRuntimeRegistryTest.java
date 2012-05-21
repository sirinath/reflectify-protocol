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

import org.abstractmeta.reflectify.ReflectifyProtocol;
import org.abstractmeta.reflectify.ReflectifyRegistry;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class ReflectifyRuntimeRegistryTest {

    public void testReflectifyRuntimeRegistry() {
        ReflectifyRegistry registry = new ReflectifyRuntimeRegistry();
        Assert.assertFalse(registry.isRegistered(Dept.class));
        ReflectifyProtocol<Dept> deptReflectify = registry.get(Dept.class);
        Assert.assertTrue(registry.isRegistered(Dept.class));
        Dept dept = deptReflectify.getProvider().get();
        deptReflectify.getMutator("name").set(dept, "name 1");
        Assert.assertEquals(dept.getName(), "name 1");
        dept.setId(12);
        Assert.assertEquals(deptReflectify.getAccessor("id").get(dept), 12);
    }

}
