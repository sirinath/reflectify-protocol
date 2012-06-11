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
import org.abstractmeta.reflectify.core.ReflectifyRegistryImpl;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class ReflectifyClassBuilderLoaderTest {

    @SuppressWarnings("unchecked")
    public void testReflectifyClassBuilderLoader() {
        ReflectifyClassBuilderLoader loader = new ReflectifyClassBuilderLoader();
        ReflectifyRegistry registry = new ReflectifyRuntimeRegistry();
        Reflectify fooBuilderReflectify = loader.load(Foo.class);
        Object builder = fooBuilderReflectify.getProvider().get();
        fooBuilderReflectify.getMutator("id").set(builder, 1);
        fooBuilderReflectify.getMutator("name").set(builder, "name 1");

        MethodInvoker<Object, Foo> buildMethodInvoker = fooBuilderReflectify.getMethodInvoker(Foo.class, "build");
        Foo foo = buildMethodInvoker.invoke(builder);
        Assert.assertEquals(foo.getId(), 1);
        Assert.assertEquals(foo.getName(), "name 1");
    }
    
    
    public static interface Foo {
        
        int getId();
        
        String getName();
        
    }
}
