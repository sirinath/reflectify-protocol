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

import org.abstractmeta.reflectify.Accessor;
import org.abstractmeta.reflectify.MethodInvoker;
import org.abstractmeta.reflectify.Mutator;
import org.abstractmeta.reflectify.Reflectify;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Abstract Reflectify Protocol class provides implementation of all base methods.
 *
 * @author Adrian Witas
 */
public abstract class AbstractReflectify<I> implements Reflectify<I> {


    private final Class<I> type;
    private final Map<String, Mutator<I, Object>> mutators;
    private final Map<String, Accessor<I, Object>> accessors;
    private final Map<String, List<MethodInvoker<I, Object>>> methods;
    private final List<Provider<I>> providers;

    private final List<String> fieldNames;

    private final Map<String, Class> fieldTypes;

    private final List<String> methodNames;

    protected AbstractReflectify(Class<I> type) {
        this.type = type;
        this.fieldNames = new ArrayList<String>();
        this.methodNames = new ArrayList<String>();
        this.fieldTypes = new HashMap<String, Class>();
        this.mutators = getMutator();
        this.accessors = getAccessors();
        this.providers = getProviders();
        this.methods = getMethods();
    }

    public List<Provider<I>> getProviders() {
        List<Provider<I>> result = new ArrayList<Provider<I>>();
        registerProviders(result);
        return result;
    }

    protected abstract void registerProviders(List<Provider<I>> result);

    protected Map<String, List<MethodInvoker<I, Object>>> getMethods() {
        Map<String, List<MethodInvoker<I, Object>>> result = new HashMap<String, List<MethodInvoker<I, Object>>>();
        registerMethodInvokers(result);
        return result;
    }

    protected abstract void registerMethodInvokers(Map<String, List<MethodInvoker<I, Object>>> methods);


    @SuppressWarnings("unchecked")
    protected void register(Map<String, List<MethodInvoker<I, Object>>> methods, String methodName, MethodInvoker methodExecutor) {
        List<MethodInvoker<I, Object>> methodForThisName = methods.get(methodName);
        if (methodForThisName == null) {
            methodForThisName = new ArrayList<MethodInvoker<I, Object>>();
            methods.put(methodName, methodForThisName);
        }
        methodNames.add(methodName);
        methodForThisName.add(methodExecutor);
    }


    protected Map<String, Accessor<I, Object>> getAccessors() {
        Map<String, Accessor<I, Object>> result = new HashMap<String, Accessor<I, Object>>();
        registerAccessors(result);
        return result;
    }

    protected abstract void registerAccessors(Map<String, Accessor<I, Object>> accessors);

    @SuppressWarnings("unchecked")
    protected void register(Map<String, Accessor<I, Object>> accessors, String fieldName, Class fieldType, Accessor accessor) {
        accessors.put(fieldName, accessor);
        this.fieldNames.add(fieldName);
        this.fieldTypes.put(fieldName, fieldType);
    }

    protected Map<String, Mutator<I, Object>> getMutator() {
        Map<String, Mutator<I, Object>> result = new HashMap<String, Mutator<I, Object>>();
        registerMutators(result);
        return result;
    }

    protected abstract void registerMutators(Map<String, Mutator<I, Object>> mutators);

    @SuppressWarnings("unchecked")
    protected void register(Map<String, Mutator<I, Object>> mutators, String fieldName, Mutator mutator) {
        mutators.put(fieldName, mutator);
    }

    @Override
    public Class<I> getType() {
        return type;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> MethodInvoker<I, T> getMethodInvoker(Class<T> methodResultType, String methodName, Class... argumentTypes) {
        MethodInvoker<I, Object> result = getMethodInvoker(methodName, argumentTypes);
        if (result == null) {
            return null;
        }
        return (MethodInvoker<I, T>) result;
    }


    @Override
    @SuppressWarnings("unchecked")
    public MethodInvoker<I, Object> getMethodInvoker(String methodName, Class... argumentTypes) {
        List<MethodInvoker<I, Object>> resultCandidate = methods.get(methodName);
        if (resultCandidate.size() == 1) {
            return resultCandidate.get(0);
        }
        String key = getMethodArguments(argumentTypes);
        for (MethodInvoker executorCandidate : resultCandidate) {
            String candidateKey = getMethodArguments(executorCandidate.getGenericParameterTypes());
            if (key.equals(candidateKey)) {
                return executorCandidate;
            }
        }
        for (MethodInvoker executorCandidate : resultCandidate) {
            if (executorCandidate.getParameterTypes().length != argumentTypes.length) continue;
            int i = 0;
            boolean match = true;
            for (Class parameters : executorCandidate.getParameterTypes()) {
                Class argumentType = argumentTypes[i++];
                if (!parameters.isAssignableFrom(argumentType)) {
                    match = false;
                    break;
                }
            }
            if (match) {
                return executorCandidate;
            }
        }
        return null;
    }


    @Override
    @SuppressWarnings("unchecked")
    public Provider<I> getProvider(Class... argumentTypes) {
        String key = getMethodArguments(argumentTypes);
        for (Provider<I> executorCandidate : providers) {
            String candidateKey = getMethodArguments(executorCandidate.getGenericParameterTypes());
            if (key.equals(candidateKey)) {
                return executorCandidate;
            }
        }
        if(type.isInterface()) {
            throw new IllegalStateException("Interface  " + type +  " has not instance provider");
        }
        throw new IllegalArgumentException("Failed to find provider matching [" + key + "] for" + type);
    }


    @Override
    public List<MethodInvoker<I, Object>> getMethodInvokers(String methodName) {
        return methods.get(methodName);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Accessor<I, T> getAccessor(Class<T> fieldType, String fieldName) {
        Accessor<I, Object> result = accessors.get(fieldName);
        if (result == null) {
            return null;
        }
        return (Accessor<I, T>) result;
    }

    @Override
    public Accessor<I, Object> getAccessor(String fieldName) {
        Accessor<I, Object>  result =  accessors.get(fieldName);
        if(result == null) {
            result = createReflectionAccessor(fieldName, type);
            if(result != null) {
                accessors.put(fieldName, result);
            }
        }

        return result;
    }



    protected Accessor<I,Object> createReflectionAccessor(final String fieldName, final Class<I> type) {
        RuntimeException runtimeException = null;
        try {
            if(! type.isInterface())  {
                final Field field = type.getField(fieldName);
                field.setAccessible(true);
                return new Accessor<I,Object>() {

                    @Override
                    public Object get(I instance) {
                        try {
                            return field.get(instance);
                        } catch (IllegalAccessException e) {
                            throw new IllegalStateException("Failed to read "  + type.getSimpleName() + "."+ fieldName, e);
                        }
                    }
                };

            }

        } catch (Exception e) {
            runtimeException = new IllegalStateException(e);
        }
        throw new IllegalStateException("Failed to lookup accessor " + fieldName + " for  "  + type.getName(), runtimeException);
    }



    @Override
    @SuppressWarnings("unchecked")
    public <T> Mutator<I, T> getMutator(Class<T> fieldType, String fieldName) {
        Mutator<I, Object> result = mutators.get(fieldName);
        if (result == null) {
            result = createReflectionMutator(fieldName, type);
            if(result == null) {
                return null;
            }
            mutators.put(fieldName, result);

        }
        return (Mutator<I, T>) result;
    }

    protected Mutator<I,Object> createReflectionMutator(final String fieldName, final Class<I> type) {
        RuntimeException runtimeException = null;
        try {
            if(! type.isInterface())  {
                final Field field = type.getField(fieldName);
                field.setAccessible(true);
                return new Mutator<I,Object>() {


                    @Override
                    public void set(I instance, Object value) {
                        try {
                            field.set(instance, value);
                        } catch (IllegalAccessException e) {
                            throw new IllegalStateException("Failed to update " + type.getSimpleName() + "." + fieldName, e);
                        }
                    }
                };

            }

        } catch (Exception e) {
            runtimeException = new IllegalStateException(e);
        }
        throw new IllegalStateException("Failed to lookup mutator " + fieldName + " for  "  + type.getName(), runtimeException);
    }

    @Override
    public Mutator<I, Object> getMutator(String fieldName) {
        return mutators.get(fieldName);
    }


    @Override
    public List<String> getFieldNames() {
        return fieldNames;
    }

    public Class getFieldType(String fieldName) {
        return fieldTypes.get(fieldName);
    }

    @Override
    public List<String> getMethodNames() {
        return methodNames;
    }

    protected String getMethodArguments(Class[] types) {
        StringBuilder result = new StringBuilder();
        for (Class type : types) {
            result.append(type);
        }
        return result.toString();
    }


    protected String getMethodArguments(Type[] types) {
        StringBuilder result = new StringBuilder();
        for (Type type : types) {
            result.append(type.toString());
        }
        return result.toString();
    }


}
