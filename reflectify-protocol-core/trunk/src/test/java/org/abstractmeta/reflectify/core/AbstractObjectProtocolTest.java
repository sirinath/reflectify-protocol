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
package org.abstractmeta.reflectify.core;

import org.abstractmeta.reflectify.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

/**
 * Represents AbstractObjectProtocolTest
 *
 * @author Adrian Witas
 */
@Test
public class AbstractObjectProtocolTest {
    
    
    public void testAbstractObjectProtocol() {
        ReflectifyProtocol<Foo> fooProtocol = new FooReflectifyProtocol();
        Foo foo = fooProtocol.getProvider().get();
        fooProtocol.getMutator(Integer.class, "id").set(foo, 10);
        Assert.assertEquals(foo.getId(), 10);
        foo.setName("abc");
        Assert.assertEquals(fooProtocol.getAccessor(String.class, "name").get(foo), "abc");
        String toString = fooProtocol.getMethodInvokder(String.class, "toString").invoke(foo);

        MethodInvoker<Foo, String> say = fooProtocol.getMethodInvokder(String.class, "say");
        say.getParameterSetter(String.class, 0).set("hello world");
        Assert.assertEquals(say.invoke(foo), "hello world");
        Assert.assertEquals(toString, "Foo{id=10, name='abc'}");
    }
   
    
    
    public static class FooReflectifyProtocol extends AbstractReflectifyProtocol<Foo> {

        protected FooReflectifyProtocol() {
            super(Foo.class);
        }

        @Override
        protected void registerProviders(List<Provider<Foo>> providers) {
            //

        }

        @Override
        protected void registerMethodInvokers(Map<String, List<MethodInvoker<Foo, Object>>> methods) {
            class FooMethodInvoker extends AbstractType implements MethodInvoker<Foo, String> {

             @Override
                public String invoke(Foo instance) {
                    return instance.toString();
                }

            }
            register(methods, "toString", new FooMethodInvoker());


            class FooSayMethodInvoker extends AbstractType implements MethodInvoker<Foo, String> {

                private String argument1;
                
                @Override
                public String invoke(Foo instance) {
                    return instance.say(argument1);
                }

                @SuppressWarnings("unchecked")
                @Override
                public <T> ParameterSetter<T> getParameterSetter(Class<T> argumentType, int argumentIndex) {
                    ParameterSetter result = null;
                    if(argumentIndex == 0) {
                        result = new ParameterSetter<String>() {

                            @Override
                            public void set(String value) {
                                argument1 = value;
                            }
                        };
                    } else {
                        throw new ArrayIndexOutOfBoundsException(String.format("Invalid argument index %s", argumentIndex));
                    }
                    return result;
                }
               
            }
            register(methods, "say", new FooSayMethodInvoker());
        }

        @Override
        protected void registerAccessors(Map<String, Accessor<Foo, Object>> accessors) {
            class FooIdAccessor implements Accessor<Foo, Integer> {
                @Override
                public Integer get(Foo instance) {
                    return instance.getId();
                }
            }
            register(accessors, "id", Integer.class,  new FooIdAccessor());

            class FooNameAccessor implements Accessor<Foo, String> {
                @Override
                public String get(Foo instance) {
                    return instance.getName();
                }
            }
            register(accessors, "name", String.class, new FooNameAccessor());

        }

        @Override
        protected void registerMutators(Map<String, Mutator<Foo, Object>> mutators) {
            class FooIdMutator implements Mutator<Foo, Integer> {

                @Override
                public void set(Foo instance, Integer value) {
                    instance.setId(value);
                }
            }
            register(mutators, "id", new FooIdMutator());

            class FooNameAccessor implements Mutator<Foo, String> {

                @Override
                public void set(Foo instance, String value) {
                    instance.setName(value);
                }
            }
            register(mutators, "name", new FooNameAccessor());
        }


        @Override
        public Provider<Foo> getProvider(Class ... argumentTypes) {
            class FooProvider extends AbstractType implements Provider<Foo> {

                @Override
                public Foo get() {
                    return new Foo();
                }
            }
            return new FooProvider();
        }

        @Override
        public Class getFieldType(String fieldName) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }
    
        
    
    public static class Foo {
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

        @Override
        public String toString() {
            return "Foo{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    '}';
        }
        
        public String say(String message) {
            return message;
        }
    }
}
