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
