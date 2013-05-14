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

import com.google.common.base.Joiner;
import org.abstractmeta.code.g.code.*;
import org.abstractmeta.code.g.config.NamingConvention;
import org.abstractmeta.code.g.config.UnitDescriptor;
import org.abstractmeta.code.g.config.loader.SourceLoader;
import org.abstractmeta.code.g.core.code.builder.JavaConstructorBuilder;
import org.abstractmeta.code.g.core.code.builder.JavaFieldBuilder;
import org.abstractmeta.code.g.core.code.builder.JavaMethodBuilder;
import org.abstractmeta.code.g.core.code.builder.JavaTypeBuilderImpl;
import org.abstractmeta.code.g.core.config.DescriptorImpl;
import org.abstractmeta.code.g.core.config.NamingConventionImpl;
import org.abstractmeta.code.g.core.config.loader.JavaSourceLoaderImpl;
import org.abstractmeta.code.g.core.diconfig.JavaTypeRendererProvider;
import org.abstractmeta.code.g.core.generator.AbstractGenerator;
import org.abstractmeta.code.g.core.internal.ParameterizedTypeImpl;
import org.abstractmeta.code.g.core.internal.SuppressWarningsImpl;
import org.abstractmeta.code.g.core.internal.TypeNameWrapper;
import org.abstractmeta.code.g.core.internal.TypeVariableImpl;
import org.abstractmeta.code.g.core.util.CodeGeneratorUtil;
import org.abstractmeta.code.g.core.util.JavaTypeUtil;
import org.abstractmeta.code.g.core.util.ReflectUtil;
import org.abstractmeta.code.g.generator.CodeGenerator;
import org.abstractmeta.code.g.generator.Context;
import org.abstractmeta.code.g.renderer.JavaTypeRenderer;
import org.abstractmeta.reflectify.*;
import org.abstractmeta.reflectify.core.AbstractMethodInvoker;
import org.abstractmeta.reflectify.core.AbstractProvider;
import org.abstractmeta.reflectify.core.AbstractReflectify;
import org.abstractmeta.reflectify.core.AbstractType;

import javax.inject.Provider;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.*;

/**
 * Builder generator.
 * It creates a build class for supplied simple class generator.
 *
 * @author Adrian Witas
 */
public class ReflectifyGenerator extends AbstractGenerator<ReflectifyGeneratorConfig> implements CodeGenerator {


    private final NamingConvention DEFAULT_NAMING_CONVENTION = new NamingConventionImpl("", "Reflectify", "reflectify");

    
    public ReflectifyGenerator() {
        this(new JavaSourceLoaderImpl(), new JavaTypeRendererProvider());
    }
    
    public ReflectifyGenerator(SourceLoader sourceLoader, Provider<JavaTypeRenderer> rendererProvider) {
        super(sourceLoader, rendererProvider);
    }



       @Override
       protected boolean isApplicable(JavaType javaType, Context context) {
           return JavaKind.CLASS.equals(javaType.getKind()) || JavaKind.INTERFACE.equals(javaType.getKind());
       }
   
       @Override
       public NamingConvention getNamingConvention(Context context) {
           return DEFAULT_NAMING_CONVENTION;
       }
   
       @Override
       public Class<ReflectifyGeneratorConfig> getSettingClass() {
           return ReflectifyGeneratorConfig.class;
       }
    

  


    @Override
        protected Collection<SourcedJavaType> generate(JavaType sourceType, Context context) {
        checkConfigurationSetting(context);
        String targetTypeName = formatTargetClassName(context, sourceType);
        Type reflectifyType = new TypeNameWrapper(sourceType.getName());
        String classSimpleName = JavaTypeUtil.getSimpleClassName(sourceType.getName(), true);
        JavaTypeBuilder protoBuilder = new JavaTypeBuilderImpl(targetTypeName);
        protoBuilder.addModifiers(JavaModifier.PUBLIC);
        protoBuilder.setSuperType(new ParameterizedTypeImpl(null, AbstractReflectify.class, reflectifyType));

        protoBuilder.addConstructor(new JavaConstructorBuilder().addModifier(JavaModifier.PUBLIC).setName(protoBuilder.getSimpleName()).addBodyLines("super(" + classSimpleName + ".class);").build());
        List<JavaMethod> methods = sourceType.getMethods();
        generateAccessors(protoBuilder, methods, reflectifyType);
        generateMutators(protoBuilder, methods, reflectifyType);
        generateMethodInvokers(protoBuilder, methods, reflectifyType);
        generateProviders(protoBuilder, sourceType, reflectifyType);
        if(sourceType.getName().indexOf('$') != -1) {
            String innerType = sourceType.getName().replace('$', '.');
            String ownerType = protoBuilder.getImporter().getSimpleTypeName(sourceType.getName());
            protoBuilder.getImporter().addTypes(new TypeNameWrapper(innerType), new TypeNameWrapper(ownerType));
  
        } else {
            protoBuilder.getImporter().addTypes(new TypeNameWrapper(sourceType.getName()));
        }
       return Arrays.asList(renderCode(protoBuilder));
    }


    protected String formatTargetClassName(Context context, JavaType sourceType) {

        return CodeGeneratorUtil.formatTargetClassName(context, sourceType.getPackageName(), sourceType.getSimpleName(), getNamingConvention(context));
    }



    protected void checkConfigurationSetting(Context context) {
        if(! context.contains(ReflectifyGeneratorConfig.class)) {
             return;
        }
        ReflectifyGeneratorConfig config = context.get(ReflectifyGeneratorConfig.class);
        if(config.isGenerateProvider()) {
            if(context.contains(UnitDescriptor.class)) {
                UnitDescriptor unitDescriptor = context.get(UnitDescriptor.class);
                if(unitDescriptor.getPostDescriptor() == null) {
                    DescriptorImpl descriptor = new DescriptorImpl();
                    descriptor.setGeneratorClass(ReflectifyProviderGenerator.class.getName());
                    unitDescriptor.setPostDescriptor(descriptor);
                }
            }
        }
    }


    private void generateProviders(JavaTypeBuilder typeBuilder, JavaType sourceType, Type reflectifyType) {
        Map<String, Integer> providerCounter = new HashMap<String, Integer>();
        JavaMethodBuilder methodBuilder = new JavaMethodBuilder();
        methodBuilder.addModifier(JavaModifier.PROTECTED).setName("registerProviders").setResultType(void.class);
        methodBuilder.addParameter("providers", new ParameterizedTypeImpl(null, List.class, new ParameterizedTypeImpl(null, Reflectify.Provider.class, reflectifyType)));
        methodBuilder.addBodyLines("\n");
        if (sourceType.getConstructors() == null) {
            return;
        }
        if (sourceType.getModifiers().contains(JavaModifier.ABSTRACT)) {
            return;
        }
        for (JavaConstructor constructor : sourceType.getConstructors()) {
            if(! constructor.getModifiers().contains(JavaModifier.PUBLIC)) continue;
            String sourceTypeSimpleName = JavaTypeUtil.getSimpleClassName(sourceType.getName(), true);
            String constructorCounterPostfix = getOccurrence(providerCounter,  JavaTypeUtil.getSimpleClassName(sourceType.getName(), false));
            String providerClassName = CodeGeneratorUtil.getClassName(JavaTypeUtil.getSimpleClassName(sourceType.getName(), false),"provider" + constructorCounterPostfix);
            JavaTypeBuilder providerClassBuilder = new JavaTypeBuilderImpl(providerClassName).setNested(true);
            methodBuilder.addNestedJavaType(providerClassBuilder);
            providerClassBuilder.addSuperInterfaces(new ParameterizedTypeImpl(null, Reflectify.Provider.class, reflectifyType));
            String parameters = Joiner.on(", ").join(getArgumentClasses(constructor.getParameters()));
            if(! parameters.isEmpty()) parameters = ", " + parameters;
            providerClassBuilder.addConstructor(new JavaConstructorBuilder().setName(providerClassName).addBodyLines(String.format("super(%s.class%s);",
                    sourceTypeSimpleName, parameters)).build());
            providerClassBuilder.setSuperType(AbstractProvider.class);
            List<Type> constructorParameterTypes = JavaTypeUtil.getParameterTypes(constructor.getParameters());
            buildArgumentSetterClasses(providerClassBuilder, reflectifyType, sourceTypeSimpleName, constructorParameterTypes);
            methodBuilder.addBodyLines(String.format(String.format("providers.add(new %s());", providerClassName)));
            JavaMethodBuilder getMethodProvider = new JavaMethodBuilder().addModifier(JavaModifier.PUBLIC).setName("get").setResultType(reflectifyType);
            String constructorParameters = Joiner.on(", ").join(getArgumentSetterMethodArgumentNames(constructorParameterTypes));
            boolean exceptionHandling = constructor.getExceptionTypes() != null && !  constructor.getExceptionTypes().isEmpty();

            if(exceptionHandling) {
                getMethodProvider.addBodyLines("try {");
            }
            getMethodProvider.addBodyLines((exceptionHandling ? "    " : "") + String.format("return new %s(%s);", sourceTypeSimpleName, constructorParameters));
            if(exceptionHandling) {
                getMethodProvider.addBodyLines("} catch(Exception e) {");
                getMethodProvider.addBodyLines(String.format("    throw new RuntimeException(\"Failed to instantiate %s\", e);", sourceTypeSimpleName));
                getMethodProvider.addBodyLines("}");
            }

            providerClassBuilder.addMethod(getMethodProvider.build());
        }
        typeBuilder.addMethod(methodBuilder.build());
    }


    protected void generateMethodInvokers(JavaTypeBuilder typeBuilder, List<JavaMethod> methods, Type reflectifyType) {
        Map<String, Integer> methodCounter = new HashMap<String, Integer>();
        JavaMethodBuilder methodBuilder = new JavaMethodBuilder();
        methodBuilder.addAnnotation(new SuppressWarningsImpl("unchecked"));
        methodBuilder.addModifier(JavaModifier.PROTECTED).setName("registerMethodInvokers").setResultType(void.class);
        methodBuilder.addParameter("methods", new ParameterizedTypeImpl(null, Map.class, String.class,
                new ParameterizedTypeImpl(null, List.class, new ParameterizedTypeImpl(null, MethodInvoker.class, reflectifyType, Object.class))));
        methodBuilder.addBodyLines("\n");
        for (JavaMethod method : methods) {
            if (!method.getModifiers().contains(JavaModifier.PUBLIC)) {
                continue;
            }
            String methodName = method.getName();
            String methodInvokerTypeNamePostfix = getOccurrence(methodCounter, methodName);
            String methodInvokerClassName = CodeGeneratorUtil.getClassName(methodName, "invoker" + methodInvokerTypeNamePostfix);
            boolean exceptionHandling = method.getExceptionTypes() != null && !  method.getExceptionTypes().isEmpty();
            buildMethodInvokerType(methodBuilder, methodName, methodInvokerClassName, ReflectUtil.getObjectType(method.getResultType()), method.getParameters(), reflectifyType, exceptionHandling);
            methodBuilder.addBodyLines(String.format(String.format("register(methods, \"%s\", new %s());", methodName, methodInvokerClassName)));

        }
        typeBuilder.addMethod(methodBuilder.build());
    }


    protected void buildArgumentSetterClasses(JavaTypeBuilder typeBuilder, Type reflectifyType, String name, List<Type> parameterType) {
        JavaMethodBuilder parameterSetterMethod = new JavaMethodBuilder();
        parameterSetterMethod.addAnnotation(new SuppressWarningsImpl("unchecked"));

        TypeVariable typeVariable = new TypeVariableImpl("RP", Object.class);
        parameterSetterMethod.setName("getParameterSetter").addModifier(JavaModifier.PUBLIC).addGenericVariables(typeVariable)
                .setResultType(new ParameterizedTypeImpl(null, ParameterSetter.class, typeVariable));

        buildArgumentSetter(typeBuilder, parameterSetterMethod, name, parameterType, reflectifyType);
        parameterSetterMethod.addParameter("parameterType", new ParameterizedTypeImpl(null, Class.class, typeVariable));
        parameterSetterMethod.addParameter("parameterIndex", int.class);
        typeBuilder.addMethod(parameterSetterMethod.build());
    }


    protected void buildMethodInvokerType(JavaMethodBuilder methodBuilder, String methodName, String methodInvokerClassName, Type resultType, List<JavaParameter> javaParameters, Type reflectifyType, boolean exceptionHandling) {
        List<Type> parameterTypes = JavaTypeUtil.getParameterTypes(javaParameters);
        JavaTypeBuilder invokerClassBuilder = new JavaTypeBuilderImpl(methodInvokerClassName).setNested(true);
        methodBuilder.addNestedJavaType(invokerClassBuilder);

        invokerClassBuilder.setSuperType(AbstractMethodInvoker.class);
        List<String> superTypeConstructorArguments = new ArrayList<String>(Arrays.asList("getType()", "\"" + methodName + "\""));
        superTypeConstructorArguments.addAll(getArgumentClasses(javaParameters));
        invokerClassBuilder.addConstructor(new JavaConstructorBuilder().setName(invokerClassBuilder.getSimpleName())
                .addBodyLines(String.format("super(%s);", Joiner.on(", ").join(superTypeConstructorArguments))).build());
        invokerClassBuilder.addSuperInterfaces(new ParameterizedTypeImpl(null, MethodInvoker.class, reflectifyType, resultType));

        JavaMethodBuilder methodInvokerClassBuilder = new JavaMethodBuilder();
        methodInvokerClassBuilder.addModifier(JavaModifier.PUBLIC).setName("invoke").setResultType(resultType);
        methodInvokerClassBuilder.addParameter("instance", reflectifyType);
        buildArgumentSetterClasses(invokerClassBuilder, reflectifyType, methodName, parameterTypes);
        String invokeMethodArgumentLiteral = Joiner.on(", ").join(getArgumentSetterMethodArgumentNames(parameterTypes));
        Set<Type> genericTypes = new HashSet<Type>();
        if(exceptionHandling) {
            methodInvokerClassBuilder.addBodyLines("try {");
        }
        String bodyIndent = (exceptionHandling ? "    " : "");
        if (Void.class.equals(resultType)) {
            methodInvokerClassBuilder.addBodyLines(bodyIndent + String.format("instance.%s(%s);", methodName, invokeMethodArgumentLiteral));
            methodInvokerClassBuilder.addBodyLines(bodyIndent + "return null;");
        } else {
            Set<Type> resultVariableType = ReflectUtil.getTypeVariables(resultType);
            genericTypes.addAll(resultVariableType);



            String cast  = resultVariableType.size() > 0 ? "(" + invokerClassBuilder.getImporter().getSimpleTypeName(resultType) + ")" :"";

            methodInvokerClassBuilder.addBodyLines(bodyIndent+ String.format("return %sinstance.%s(%s);", cast, methodName, invokeMethodArgumentLiteral));

        }
        if(exceptionHandling) {
            methodInvokerClassBuilder.addBodyLines("} catch(Exception e) {");
            methodInvokerClassBuilder.addBodyLines(String.format("    throw new RuntimeException(\"Failed to invoke %s\", e);", methodName));
            methodInvokerClassBuilder.addBodyLines("}");
        }

        invokerClassBuilder.addMethod(methodInvokerClassBuilder.build());
       for(JavaField field: invokerClassBuilder.getFields()) {
            Type fieldType = field.getType();
            genericTypes.addAll(ReflectUtil.getTypeVariables(fieldType));
            invokerClassBuilder.getGenericTypeVariables().put(invokerClassBuilder.getImporter().getSimpleTypeName(fieldType), Object.class);

        }



        invokerClassBuilder.addGenericTypeArguments(genericTypes);

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

 
    private void buildArgumentSetter(JavaTypeBuilder nestedClassBuilder, JavaMethodBuilder parameterSetterMethod, String methodName, List<Type> genericParameterTypes, Type reflectifyType) {
        List<String> parameterSimpleTypeNames = new ArrayList<String>();
        if (genericParameterTypes != null && genericParameterTypes.size() > 0) {
            parameterSetterMethod.addBodyLines("\nswitch(parameterIndex) {");
            for (int i = 0; i < genericParameterTypes.size(); i++) {
                Type fileType = ReflectUtil.getObjectType(genericParameterTypes.get(i));
                parameterSimpleTypeNames.add(ReflectUtil.getRawClass(fileType).getSimpleName());
                String fieldName = "parameter" + i;
                nestedClassBuilder.addField(new JavaFieldBuilder().setName(fieldName).setType(genericParameterTypes.get(i)).build());
                String parameterSetterClassName = CodeGeneratorUtil.getClassName(fieldName, "Setter");
                JavaTypeBuilder parameterSetterClass = new JavaTypeBuilderImpl(parameterSetterClassName).setNested(true);

                parameterSetterMethod.addNestedJavaType(parameterSetterClass);



                parameterSetterClass.setSuperType(AbstractType.class);
                parameterSetterClass.addSuperInterfaces(new ParameterizedTypeImpl(null, ParameterSetter.class, fileType));
                JavaMethodBuilder methodBuilder = new JavaMethodBuilder().addModifier(JavaModifier.PUBLIC).setName("set").setResultType(void.class);
                methodBuilder.addParameter("value", fileType);
                methodBuilder.addBodyLines(fieldName + " = value;");
                parameterSetterClass.addMethod(methodBuilder.build());
                parameterSetterMethod.addBodyLines("    case   " + i + ": return (ParameterSetter<RP>) new " + parameterSetterClassName + "();");
            }
            nestedClassBuilder.getImporter().addTypes(ArrayIndexOutOfBoundsException.class);
            parameterSetterMethod.addBodyLines("}");

        }
        nestedClassBuilder.getImporter().addTypes(ArrayIndexOutOfBoundsException.class);
        String ownerType;
        if (reflectifyType instanceof TypeNameWrapper) {
            ownerType = TypeNameWrapper.class.cast(reflectifyType).getTypeName();
        } else {
            ownerType = ReflectUtil.getRawClass(reflectifyType).getSimpleName();
        }
        parameterSetterMethod.addBodyLines(String.format("throw new %s(\"Invalid index parameter \" + parameterIndex + \" for %s.%s(%s)\");",
                ArrayIndexOutOfBoundsException.class.getSimpleName(),
                ownerType,
                methodName,
                Joiner.on(", ").join(parameterSimpleTypeNames)
        ));


    }


    protected void generateAccessors(JavaTypeBuilder typeBuilder, List<JavaMethod> methods, Type reflectifyType) {
        JavaMethodBuilder methodBuilder = new JavaMethodBuilder();
        methodBuilder.addModifier(JavaModifier.PROTECTED).setName("registerAccessors").setResultType(void.class);
        methodBuilder.addParameter("accessors", new ParameterizedTypeImpl(null, Map.class, String.class, new ParameterizedTypeImpl(null, Accessor.class, reflectifyType, Object.class)));
        methodBuilder.addBodyLines("\n");
        for (JavaMethod method : methods) {
            if (!method.getModifiers().contains(JavaModifier.PUBLIC)) {
                continue;
            }
            String methodName = method.getName();
            if (!((methodName.startsWith("get") || methodName.startsWith("is")) &&  method.getParameters().size() == 0)) {
                continue;
            }

            String fieldName = CodeGeneratorUtil.extractFieldNameFromMethodName(method.getName());
            Type fieldType = ReflectUtil.getObjectType(method.getResultType());
            Class fieldRawClass = ReflectUtil.getRawClass(fieldType);
            String accessorClassName = CodeGeneratorUtil.getClassName(fieldName, "accessor");
            JavaTypeBuilder nestedClassBuilder = new JavaTypeBuilderImpl(accessorClassName).setNested(true);
            methodBuilder.addNestedJavaType(nestedClassBuilder);
            nestedClassBuilder.addSuperInterfaces(new ParameterizedTypeImpl(null, Accessor.class, reflectifyType, fieldType));
            JavaMethodBuilder accessorBuilder = new JavaMethodBuilder();
            accessorBuilder.addModifier(JavaModifier.PUBLIC).setName("get").setResultType(fieldType);
            accessorBuilder.addParameter("instance", reflectifyType);
            accessorBuilder.addBodyLines("return instance." + methodName + "();");
            String fieldSimpleName;
            if (fieldRawClass.isArray()) {
                fieldSimpleName = JavaTypeUtil.getSimpleClassName(fieldRawClass.getComponentType().getName(), true) + " []";
            } else {
                fieldSimpleName = JavaTypeUtil.getSimpleClassName(fieldRawClass.getName(), true);
            }

            nestedClassBuilder.addMethod(accessorBuilder.build());
            methodBuilder.addBodyLines("register(accessors, \"" + fieldName + "\", " + fieldSimpleName + ".class , new " + accessorClassName + "());");
        }

        typeBuilder.addMethod(methodBuilder.build());
    }


    protected void generateMutators(JavaTypeBuilder typeBuilder, List<JavaMethod> methods, Type reflectifyType) {
        JavaMethodBuilder methodBuilder = new JavaMethodBuilder();
        methodBuilder.addModifier(JavaModifier.PROTECTED).setName("registerMutators").setResultType(void.class);
        methodBuilder.addParameter("mutators", new ParameterizedTypeImpl(null, Map.class, String.class, new ParameterizedTypeImpl(null, Mutator.class, reflectifyType, Object.class)));
        methodBuilder.addBodyLines("\n");
        for (JavaMethod method : methods) {
            if (!method.getModifiers().contains(JavaModifier.PUBLIC)) {
                continue;
            }
            String methodName = method.getName();
            if (!methodName.startsWith("set") || method.getParameters().size() != 1) {
                continue;
            }
            String fieldName = CodeGeneratorUtil.extractFieldNameFromMethodName(method.getName());
            Type fieldType = ReflectUtil.getObjectType(method.getParameters().get(0).getType());
            String accessorClassName = CodeGeneratorUtil.getClassName(fieldName, "mutator");
            JavaTypeBuilder nestedClassBuilder = new JavaTypeBuilderImpl(accessorClassName).setNested(true);
            methodBuilder.addNestedJavaType(nestedClassBuilder);
            nestedClassBuilder.addSuperInterfaces(new ParameterizedTypeImpl(null, Mutator.class, reflectifyType, fieldType));
            JavaMethodBuilder accessorBuilder = new JavaMethodBuilder();
            accessorBuilder.addModifier(JavaModifier.PUBLIC).setName("set").setResultType(void.class);
            accessorBuilder.addParameter("instance", reflectifyType).addParameter("value", fieldType);
            accessorBuilder.addBodyLines("instance." + methodName + "(value);");

            nestedClassBuilder.addMethod(accessorBuilder.build());
            methodBuilder.addBodyLines("register(mutators, \"" + fieldName + "\", new " + accessorClassName + "());");
        }
        typeBuilder.addMethod(methodBuilder.build());
    }


    protected List<String> getArgumentClasses(List<JavaParameter> javaParameters) {
        List<String> result = new ArrayList<String>();
        for (JavaParameter javaParameter: javaParameters) {
            Type type = javaParameter.getType();
            String typeName;
            if (type instanceof TypeNameWrapper) {
                typeName = TypeNameWrapper.class.cast(type).getTypeName();

            } else {
                typeName = ReflectUtil.getRawClass(type).getName();
            }
            result.add(JavaTypeUtil.getSimpleClassName(typeName, true)  + ".class");

        }
        return result;
    }


   
}
