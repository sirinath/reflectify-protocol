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

import org.abstractmeta.code.g.code.JavaConstructor;
import org.abstractmeta.code.g.code.JavaMethod;
import org.abstractmeta.code.g.code.JavaType;
import org.abstractmeta.code.g.code.JavaTypeRegistry;
import org.abstractmeta.code.g.core.code.builder.JavaConstructorBuilder;
import org.abstractmeta.code.g.core.code.builder.JavaFieldBuilder;
import org.abstractmeta.code.g.core.code.builder.JavaMethodBuilder;
import org.abstractmeta.code.g.core.code.builder.JavaTypeBuilder;
import org.abstractmeta.code.g.core.internal.ParameterizedTypeImpl;
import org.abstractmeta.code.g.core.internal.SuppressWarningsImpl;
import org.abstractmeta.code.g.core.internal.TypeNameWrapper;
import org.abstractmeta.code.g.core.internal.TypeVariableImpl;
import org.abstractmeta.code.g.core.plugin.AbstractGeneratorPlugin;
import org.abstractmeta.code.g.core.util.JavaTypeUtil;
import org.abstractmeta.code.g.core.util.ReflectUtil;
import org.abstractmeta.code.g.core.util.StringUtil;
import org.abstractmeta.code.g.config.Descriptor;
import org.abstractmeta.code.g.plugin.CodeGeneratorPlugin;
import org.abstractmeta.reflectify.*;
import org.abstractmeta.reflectify.core.AbstractType;
import org.abstractmeta.reflectify.core.AbstractMethodInvoker;
import org.abstractmeta.reflectify.core.AbstractProvider;
import org.abstractmeta.reflectify.core.AbstractReflectifyProtocol;
import com.google.common.base.CaseFormat;
import com.google.common.base.Joiner;

import javax.inject.Provider;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Builder plugin.
 * It creates a build class for supplied simple class plugin.
 *
 * @author Adrian Witas
 */
public class ReflectifyGenerator extends AbstractGeneratorPlugin implements CodeGeneratorPlugin {

    public List<String> generate(Collection<String> sourceTypeNames, JavaTypeRegistry registry, Descriptor descriptor) {
        List<String> result = super.generate(sourceTypeNames, registry, descriptor);
        buildProvider(result, registry, descriptor);
        return result;
    }

    protected String getTargetTypeName(JavaType sourceType, Descriptor descriptor, JavaTypeRegistry registry) {
        String buildResultTypeName = JavaTypeUtil.getSuperTypeName(sourceType);
        String buildResultSimpleClassName = JavaTypeUtil.getSimpleClassName(buildResultTypeName, true);
        return getTargetTypeName(buildResultSimpleClassName, descriptor, registry);
    }

    private void buildProvider(List<String> result, JavaTypeRegistry registry, Descriptor descriptor) {
        if (result.size() == 0) {
            return;
        }
        JavaTypeBuilder registryTypeBuilder = new JavaTypeBuilder();
        registryTypeBuilder.addImportType(Arrays.class);
        String targetPackage = descriptor.getTargetPackage();
        if(targetPackage == null) {
            targetPackage = StringUtil.substringBeforeLastIndexOf(descriptor.getSourceClass(), ".");
        }
        String parentPackage = StringUtil.substringAfterLastIndexOf(targetPackage, ".");
        String providerTypeName = targetPackage + "." + StringUtil.format(CaseFormat.UPPER_CAMEL, parentPackage, "reflectifyProvider", CaseFormat.LOWER_CAMEL);
        Type resultType = new ParameterizedTypeImpl(null, List.class, ReflectifyProtocol.class);
        registryTypeBuilder.addModifier("public").setTypeName(providerTypeName)
                .addSuperInterface(new ParameterizedTypeImpl(null, Provider.class, resultType));
        JavaMethodBuilder methodBuilder = new JavaMethodBuilder().addModifier("public")
                .setName("get").setResultType(resultType);
        List<String> generatedTypes = new ArrayList<String>();
        for (String generatedType : result) {
            registryTypeBuilder.addImportType(new TypeNameWrapper(generatedType));
            generatedTypes.add("\n    new " + JavaTypeUtil.getSimpleClassName(generatedType) + "()");
        }
        methodBuilder.addBody(String.format("return Arrays.<%s>asList(%s);", ReflectifyProtocol.class.getSimpleName(), Joiner.on(",").join(generatedTypes)));
        registryTypeBuilder.addMethod(methodBuilder.build());
        registry.register(registryTypeBuilder.build());
        result.add(registryTypeBuilder.getName());
    }

    @Override
    protected boolean isApplicable(JavaType sourceType) {
        return true;
    }

    @Override
    protected JavaTypeBuilder generateType(JavaType sourceType, JavaTypeRegistry registry,String targetTypeName, Descriptor descriptor) {
        Type reflectifyType = new TypeNameWrapper(sourceType.getName());
        String classSimpleName = JavaTypeUtil.getSimpleClassName(sourceType.getName());
        JavaTypeBuilder protoBuilder = new JavaTypeBuilder();
        protoBuilder.addModifier("public");
        protoBuilder.setTypeName(targetTypeName);
        protoBuilder.setSuperType(new ParameterizedTypeImpl(null, AbstractReflectifyProtocol.class, reflectifyType));
        protoBuilder.addConstructor(new JavaConstructorBuilder().addModifier("public").setName(protoBuilder.getSimpleName()).addBody("super(" + classSimpleName + ".class);").build());
        List<JavaMethod> methods = sourceType.getMethods();
        generateAccessors(protoBuilder, methods, reflectifyType);
        generateMutators(protoBuilder, methods, reflectifyType);
        generateMethodInvokers(protoBuilder, methods, reflectifyType);
        generateProviders(protoBuilder, sourceType, reflectifyType);
        return protoBuilder;
    }

    private void generateProviders(JavaTypeBuilder typeBuilder, JavaType sourceType, Type reflectifyType) {
        Map<String, Integer> providerCounter = new HashMap<String, Integer>();
        JavaMethodBuilder methodBuilder = new JavaMethodBuilder();
        methodBuilder.addModifier("protected").setName("registerProviders").setResultType(void.class);
        methodBuilder.addParameter("providers", new ParameterizedTypeImpl(null, List.class, new ParameterizedTypeImpl(null, ReflectifyProtocol.Provider.class, reflectifyType)));
        methodBuilder.addBody("\n");
        if (sourceType.getConstructors() == null) {
            return;
        }
        if (sourceType.getModifiers().contains("abstract")) {
            return;
        }
        for (JavaConstructor constructor : sourceType.getConstructors()) {
            String constructorCounterPostfix = getOccurrence(providerCounter, sourceType.getSimpleName());
            String providerClassName = StringUtil.format(CaseFormat.UPPER_CAMEL, sourceType.getSimpleName(), "provider" + constructorCounterPostfix, CaseFormat.LOWER_CAMEL);
            JavaTypeBuilder providerClassBuilder = methodBuilder.addNestedJavaType();
            providerClassBuilder.setName(providerClassName).addSuperInterface(
                    new ParameterizedTypeImpl(null, ReflectifyProtocol.Provider.class, reflectifyType));
            String parameters = Joiner.on(", ").join(getArgumentClasses(constructor.getParameterTypes()));
            if(! parameters.isEmpty()) parameters = ", " + parameters;
            providerClassBuilder.addConstructor(new JavaConstructorBuilder().setName(providerClassName).addBody(String.format("super(%s.class%s);",
                    sourceType.getSimpleName(), parameters)).build());
            providerClassBuilder.setSuperType(AbstractProvider.class);
            buildArgumentSetterClasses(providerClassBuilder, reflectifyType, sourceType.getSimpleName(), constructor.getParameterTypes());
            methodBuilder.addBody(String.format(String.format("providers.add(new %s());", providerClassName)));
            JavaMethodBuilder getMethodProvider = new JavaMethodBuilder().addModifier("public").setName("get").setResultType(reflectifyType);
            String constructorParameters = Joiner.on(", ").join(getArgumentSetterMethodArgumentNames(constructor.getParameterTypes()));
            getMethodProvider.addBody(String.format("return new %s(%s);", sourceType.getSimpleName(), constructorParameters));
            providerClassBuilder.addMethod(getMethodProvider.build());
        }
        typeBuilder.addMethod(methodBuilder.build());
    }


    protected void generateMethodInvokers(JavaTypeBuilder typeBuilder, List<JavaMethod> methods, Type reflectifyType) {
        Map<String, Integer> methodCounter = new HashMap<String, Integer>();
        JavaMethodBuilder methodBuilder = new JavaMethodBuilder();
        methodBuilder.addModifier("protected").setName("registerMethodInvokers").setResultType(void.class);
        methodBuilder.addParameter("methods", new ParameterizedTypeImpl(null, Map.class, String.class,
                new ParameterizedTypeImpl(null, List.class, new ParameterizedTypeImpl(null, MethodInvoker.class, reflectifyType, Object.class))));
        methodBuilder.addBody("\n");
        for (JavaMethod method : methods) {
            if (!method.getModifiers().contains("public")) {
                continue;
            }
            String methodName = method.getName();
            String methodInvokerTypeNamePostfix = getOccurrence(methodCounter, methodName);
            String methodInvokerClassName = StringUtil.format(CaseFormat.UPPER_CAMEL, methodName, "invoker" + methodInvokerTypeNamePostfix, CaseFormat.LOWER_CAMEL);
            buildMethodInvokerType(methodBuilder, methodName, methodInvokerClassName, ReflectUtil.getObjectType(method.getResultType()), method.getParameterTypes(), reflectifyType);
            methodBuilder.addBody(String.format(String.format("register(methods, \"%s\", new %s());", methodName, methodInvokerClassName)));

        }
        typeBuilder.addMethod(methodBuilder.build());
    }


    protected void buildArgumentSetterClasses(JavaTypeBuilder typeBuilder, Type reflectifyType, String name, List<Type> parameterType) {
        JavaMethodBuilder parameterSetterMethod = new JavaMethodBuilder();
        parameterSetterMethod.addAnnotation(new SuppressWarningsImpl("unchecked"));
        parameterSetterMethod.setName("getParameterSetter").addModifier("public").addModifier("<T>")
                .setResultType(new ParameterizedTypeImpl(null, ParameterSetter.class, new TypeVariableImpl("T")));

        buildArgumentSetter(typeBuilder, parameterSetterMethod, name, parameterType, reflectifyType);
        parameterSetterMethod.addParameter("parameterType", new ParameterizedTypeImpl(null, Class.class, new TypeVariableImpl("T")));
        parameterSetterMethod.addParameter("parameterIndex", int.class);
        typeBuilder.addMethod(parameterSetterMethod.build());
    }


    protected void buildMethodInvokerType(JavaMethodBuilder methodBuilder, String methodName, String methodInvokerClassName, Type resultType, List<Type> parameterTypes, Type reflectifyType) {
        JavaTypeBuilder invokerClassBuilder = methodBuilder.addNestedJavaType();
        invokerClassBuilder.setName(methodInvokerClassName).setSuperType(AbstractMethodInvoker.class);
        List<String> superTypeConstructorArguments = new ArrayList<String>(Arrays.asList("getType()", "\"" + methodName + "\""));
        superTypeConstructorArguments.addAll(getArgumentClasses(parameterTypes));
        invokerClassBuilder.addConstructor(new JavaConstructorBuilder().setName(invokerClassBuilder.getSimpleName())
                .addBody(String.format("super(%s);", Joiner.on(", ").join(superTypeConstructorArguments))).build());
        invokerClassBuilder.addSuperInterface(new ParameterizedTypeImpl(null, MethodInvoker.class, reflectifyType, resultType));

        JavaMethodBuilder methodInvokerClassBuilder = new JavaMethodBuilder();
        methodInvokerClassBuilder.addModifier("public").setName("invoke").setResultType(resultType);
        methodInvokerClassBuilder.addParameter("instance", reflectifyType);
        buildArgumentSetterClasses(invokerClassBuilder, reflectifyType, methodName, parameterTypes);
        String invokeMethodArgumentLiteral = Joiner.on(", ").join(getArgumentSetterMethodArgumentNames(parameterTypes));
        if (Void.class.equals(resultType)) {
            methodInvokerClassBuilder.addBody(String.format("instance.%s(%s);", methodName, invokeMethodArgumentLiteral));
            methodInvokerClassBuilder.addBody("return null;");
        } else {
            methodInvokerClassBuilder.addBody(String.format("return instance.%s(%s);", methodName, invokeMethodArgumentLiteral));

        }
        invokerClassBuilder.addMethod(methodInvokerClassBuilder.build());
    }


    protected String getOccurrence(Map<String, Integer> counters, String name) {
        if (!counters.containsKey(name)) {
            counters.put(name, 0);
        } else {
            int methodOccurrence = counters.get(name);
            counters.put(name, ++methodOccurrence);
        }
        return String.valueOf(counters.get(name));
    }


    protected List<String> getArgumentSetterMethodArgumentNames(List<Type> parametersTypes) {
        List<String> result = new ArrayList<String>();
        if (parametersTypes == null || parametersTypes.size() == 0) {
            return result;
        }
        for (int i = 0; i < parametersTypes.size(); i++) {
            result.add("parameter" + i);
        }
        return result;
    }

    protected List<String> getArgumentClasses(List<Type> types) {
        List<String> result = new ArrayList<String>();
        for (Type type : types) {
            if (type instanceof TypeNameWrapper) {
                String simpleClassName = JavaTypeUtil.getSimpleClassName(TypeNameWrapper.class.cast(type).getTypeName());
                result.add(simpleClassName + ".class");

            } else {
                result.add(ReflectUtil.getRawClass(type).getSimpleName() + ".class");
            }

        }
        return result;
    }


    private void buildArgumentSetter(JavaTypeBuilder nestedClassBuilder, JavaMethodBuilder parameterSetterMethod, String methodName, List<Type> genericParameterTypes, Type reflectifyType) {
        List<String> parameterSimpleTypeNames = new ArrayList<String>();
        if (genericParameterTypes != null && genericParameterTypes.size() > 0) {
            parameterSetterMethod.addBody("\nswitch(parameterIndex) {");
            for (int i = 0; i < genericParameterTypes.size(); i++) {
                Type fileType = ReflectUtil.getObjectType(genericParameterTypes.get(i));
                parameterSimpleTypeNames.add(ReflectUtil.getRawClass(fileType).getSimpleName());
                String fieldName = "parameter" + i;
                nestedClassBuilder.addField(new JavaFieldBuilder().setName(fieldName).setType(genericParameterTypes.get(i)).build());
                String parameterSetterClassName = StringUtil.format(CaseFormat.UPPER_CAMEL, fieldName, "Setter", CaseFormat.LOWER_CAMEL);
                JavaTypeBuilder parameterSetterClass = parameterSetterMethod.addNestedJavaType();
                parameterSetterClass.setSuperType(AbstractType.class);
                parameterSetterClass.setName(parameterSetterClassName).addSuperInterface(new ParameterizedTypeImpl(null, ParameterSetter.class, fileType));
                JavaMethodBuilder methodBuilder = new JavaMethodBuilder().addModifier("public").setName("set").setResultType(void.class);
                methodBuilder.addParameter("value", fileType);
                methodBuilder.addBody(fieldName + " = value;");
                parameterSetterClass.addMethod(methodBuilder.build());
                parameterSetterMethod.addBody("    case   " + i + ": return (ParameterSetter<T>) new " + parameterSetterClassName + "();");
            }
            nestedClassBuilder.addImportType(ArrayIndexOutOfBoundsException.class);
            parameterSetterMethod.addBody("}");

        }
        nestedClassBuilder.addImportType(ArrayIndexOutOfBoundsException.class);
        String ownerType;
        if (reflectifyType instanceof TypeNameWrapper) {
            ownerType = TypeNameWrapper.class.cast(reflectifyType).getTypeName();
        } else {
            ownerType = ReflectUtil.getRawClass(reflectifyType).getSimpleName();
        }
        parameterSetterMethod.addBody(String.format("throw new %s(\"Invalid index parameter \" + parameterIndex + \" for %s.%s(%s)\");",
                ArrayIndexOutOfBoundsException.class.getSimpleName(),
                ownerType,
                methodName,
                Joiner.on(", ").join(parameterSimpleTypeNames)
        ));


    }


    protected void generateAccessors(JavaTypeBuilder typeBuilder, List<JavaMethod> methods, Type reflectifyType) {
        JavaMethodBuilder methodBuilder = new JavaMethodBuilder();
        methodBuilder.addModifier("protected").setName("registerAccessors").setResultType(void.class);
        methodBuilder.addParameter("accessors", new ParameterizedTypeImpl(null, Map.class, String.class, new ParameterizedTypeImpl(null, Accessor.class, reflectifyType, Object.class)));
        methodBuilder.addBody("\n");
        for (JavaMethod method : methods) {
            if (!method.getModifiers().contains("public")) {
                continue;
            }
            String methodName = method.getName();
            if (!methodName.startsWith("get") || method.getParameterTypes().size() > 0) {
                continue;
            }
            String fieldName = ReflectUtil.extractFieldNameFromMethodName(method.getName());
            Type fieldType = ReflectUtil.getObjectType(method.getResultType());
            Class fieldRawClass = ReflectUtil.getRawClass(fieldType);
            JavaTypeBuilder nestedClassBuilder = methodBuilder.addNestedJavaType();
            String accessorClassName = StringUtil.format(CaseFormat.UPPER_CAMEL, fieldName, "accessor", CaseFormat.LOWER_CAMEL);
            nestedClassBuilder.setName(accessorClassName);
            nestedClassBuilder.addSuperInterface(new ParameterizedTypeImpl(null, Accessor.class, reflectifyType, fieldType));
            JavaMethodBuilder accessorBuilder = new JavaMethodBuilder();
            accessorBuilder.addModifier("public").setName("get").setResultType(fieldType);
            accessorBuilder.addParameter("instance", reflectifyType);
            accessorBuilder.addBody("return instance." + methodName + "();");
            String fieldSimpleName;
            if (fieldRawClass.isArray()) {
                fieldSimpleName = fieldRawClass.getComponentType().getSimpleName() + " []";
            } else {
                fieldSimpleName = fieldRawClass.getSimpleName();
            }

            nestedClassBuilder.addMethod(accessorBuilder.build());
            methodBuilder.addBody("register(accessors, \"" + fieldName + "\", " + fieldSimpleName + ".class , new " + accessorClassName + "());");
        }

        typeBuilder.addMethod(methodBuilder.build());
    }


    protected void generateMutators(JavaTypeBuilder typeBuilder, List<JavaMethod> methods, Type reflectifyType) {
        JavaMethodBuilder methodBuilder = new JavaMethodBuilder();
        methodBuilder.addModifier("protected").setName("registerMutators").setResultType(void.class);
        methodBuilder.addParameter("mutators", new ParameterizedTypeImpl(null, Map.class, String.class, new ParameterizedTypeImpl(null, Mutator.class, reflectifyType, Object.class)));
        methodBuilder.addBody("\n");
        for (JavaMethod method : methods) {
            if (!method.getModifiers().contains("public")) {
                continue;
            }
            String methodName = method.getName();
            if (!methodName.startsWith("set") || method.getParameterTypes().size() != 1) {
                continue;
            }
            String fieldName = ReflectUtil.extractFieldNameFromMethodName(method.getName());
            Type fieldType = ReflectUtil.getObjectType(method.getParameterTypes().get(0));
            JavaTypeBuilder nestedClassBuilder = methodBuilder.addNestedJavaType();
            String accessorClassName = StringUtil.format(CaseFormat.UPPER_CAMEL, fieldName, "mutator", CaseFormat.LOWER_CAMEL);
            nestedClassBuilder.setName(accessorClassName);
            nestedClassBuilder.addSuperInterface(new ParameterizedTypeImpl(null, Mutator.class, reflectifyType, fieldType));
            JavaMethodBuilder accessorBuilder = new JavaMethodBuilder();
            accessorBuilder.addModifier("public").setName("set").setResultType(void.class);
            accessorBuilder.addParameter("instance", reflectifyType).addParameter("value", fieldType);
            accessorBuilder.addBody("instance." + methodName + "(value);");

            nestedClassBuilder.addMethod(accessorBuilder.build());
            methodBuilder.addBody("register(mutators, \"" + fieldName + "\", new " + accessorClassName + "());");
        }
        typeBuilder.addMethod(methodBuilder.build());
    }

    @Override
    public Map<String, String> getOptions() {
        Map<String, String> result = new HashMap<String, String>();
        result.put("targetPackage", "meta");
        result.put("targetPostfix", "Meta");
        return result;
    }
}
