package org.abstractmeta.reflectify.generator;

import com.google.common.base.Joiner;
import org.abstractmeta.code.g.code.*;
import org.abstractmeta.code.g.config.NamingConvention;
import org.abstractmeta.code.g.config.loader.SourceLoader;
import org.abstractmeta.code.g.core.code.builder.JavaMethodBuilder;
import org.abstractmeta.code.g.core.code.builder.JavaTypeBuilderImpl;
import org.abstractmeta.code.g.core.config.loader.JavaSourceLoaderImpl;
import org.abstractmeta.code.g.core.diconfig.JavaTypeRendererProvider;
import org.abstractmeta.code.g.core.generator.AbstractGenerator;
import org.abstractmeta.code.g.core.internal.ParameterizedTypeImpl;
import org.abstractmeta.code.g.core.util.JavaTypeUtil;
import org.abstractmeta.code.g.generator.CodeGenerator;
import org.abstractmeta.code.g.generator.Context;
import org.abstractmeta.code.g.renderer.JavaTypeRenderer;
import org.abstractmeta.reflectify.Reflectify;

import javax.inject.Provider;
import java.lang.reflect.Type;
import java.util.*;

/**
 *
 */
public class ReflectifyProviderGenerator extends AbstractGenerator<ReflectifyGeneratorConfig> implements CodeGenerator {

    public static final String REFLECTIFY_PROVIDER = "ReflectifyProvider";

    public ReflectifyProviderGenerator() {
        this(new JavaSourceLoaderImpl(), new JavaTypeRendererProvider());
    }

    public ReflectifyProviderGenerator(SourceLoader sourceLoader, Provider<JavaTypeRenderer> rendererProvider) {
        super(sourceLoader, rendererProvider);
    }

    @Override
    protected Collection<SourcedJavaType> generate(JavaType sourceType, Context context) {
        throw new UnsupportedOperationException();
    }


    @Override
    public List<CompiledJavaType> generate(Context context) {
        if (!isApplicable(null, context)) {
            return Collections.emptyList();
        }
        List<CompiledJavaType> reflectifies = getReflectifies(context);
        String packageName = extractPackageName(reflectifies);
        Type resultType = new ParameterizedTypeImpl(null, Collection.class, Reflectify.class);
        JavaTypeBuilder registryTypeBuilder = new JavaTypeBuilderImpl(packageName + "." + REFLECTIFY_PROVIDER);
        registryTypeBuilder.addModifiers(JavaModifier.PUBLIC)
                .addSuperInterfaces(new ParameterizedTypeImpl(null, Provider.class, resultType));
        registryTypeBuilder.getImporter().addTypes(Arrays.class);
        JavaMethodBuilder methodBuilder = new JavaMethodBuilder().addModifier(JavaModifier.PUBLIC).setName("get").setResultType(resultType);

        List<String> generatedTypes = new ArrayList<String>();
        if(reflectifies.size() == 0) {
            throw new IllegalStateException("Failed to build type");
        }
        for (CompiledJavaType generatedType : reflectifies) {
            registryTypeBuilder.getImporter().addTypes(generatedType.getCompiledType());
            generatedTypes.add("\n    new " + JavaTypeUtil.getSimpleClassName(generatedType.getCompiledType().getSimpleName(), true) + "()");
        }
        methodBuilder.addBodyLines(String.format("return Arrays.<%s>asList(%s);", Reflectify.class.getSimpleName(), Joiner.on(",").join(generatedTypes)));
        registryTypeBuilder.addMethod(methodBuilder.build());
        SourcedJavaType result = renderCode(registryTypeBuilder);

        return compileGeneratedTypes(Arrays.asList(result), context);
    }

    /**
     * Extract the shortest package name
     *
     * @param reflectifies list of compiled java type implementing reflecify interface
     * @return pakcage name
     */
    protected String extractPackageName(List<CompiledJavaType> reflectifies) {
        String result = "";
        for (CompiledJavaType reflectify : reflectifies) {
            String packageName = reflectify.getType().getPackageName();
            if (result.isEmpty() || packageName.length() < result.length()) {
                result = packageName;
            }
        }
        return result;
    }



    @Override
    protected boolean isApplicable(JavaType javaType, Context context) {
        return getReflectifies(context) != null;
    }

    protected List<CompiledJavaType> getReflectifies(Context context) {
        CompiledJavaTypeRegistry registry = context.getOptional(CompiledJavaTypeRegistry.class);
        if (registry == null) return Collections.emptyList();
        List<CompiledJavaType> reflectifies = new ArrayList<CompiledJavaType>();
        for (CompiledJavaType compiledJavaType : registry.get()) {
            if (Reflectify.class.isAssignableFrom(compiledJavaType.getCompiledType())) {
                reflectifies.add(compiledJavaType);
            }
        }
        return reflectifies;
    }

    @Override
    public NamingConvention getNamingConvention(Context context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Class<ReflectifyGeneratorConfig> getSettingClass() {
        return ReflectifyGeneratorConfig.class;
    }
}
