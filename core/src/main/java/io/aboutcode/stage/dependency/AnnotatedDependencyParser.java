package io.aboutcode.stage.dependency;

import io.aboutcode.stage.util.FieldAnalysis;
import io.aboutcode.stage.util.ThrowingFunction;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class AnnotatedDependencyParser {
    private AnnotatedDependencyParser() {
    }

    private static boolean isParameterAnnotated(Field field) {
        return getParameterAnnotation(field) != null;
    }

    private static Resolved getParameterAnnotation(Field field) {
        return field.getAnnotation(Resolved.class);
    }

    private static DependencyAware asDependencyAware(Field field, Object targetObject) {
        Resolved annotation = getParameterAnnotation(field);

        FieldAnalysis fieldAnalysis = FieldAnalysis.of(field, targetObject);

        DependencyAware result;
        switch (fieldAnalysis.getFieldType()) {
            case ARRAY:
                result = array(fieldAnalysis);
                break;
            case COLLECTION:
                result = collection(fieldAnalysis);
                break;
            case SINGLE_VALUE:
                result = singleValue(annotation, fieldAnalysis);
                break;
            default:
                result = null;
        }
        return result;
    }

    private static DependencyAware array(FieldAnalysis fieldAnalysis) {
        //noinspection unchecked
        return new AnnotatedDependencyAware(fieldAnalysis, context ->
                context.retrieveDependencies(fieldAnalysis.getSpecificClass()).toArray()
        );
    }

    private static DependencyAware collection(FieldAnalysis fieldAnalysis) {
        //noinspection unchecked
        return new AnnotatedDependencyAware(fieldAnalysis, context ->
                context.retrieveDependencies(fieldAnalysis.getSpecificClass())
        );
    }

    private static DependencyAware singleValue(Resolved annotation, FieldAnalysis fieldAnalysis) {
        return new AnnotatedDependencyAware(fieldAnalysis, context -> {
            Object identifier = identifier(annotation);
            Object dependency;
            if (identifier == null) {
                //noinspection unchecked
                dependency = context.retrieveDependency(fieldAnalysis.getSpecificClass(),
                                                        mandatory(annotation));
            } else {
                //noinspection unchecked
                dependency = context.retrieveDependency(identifier,
                                                        fieldAnalysis.getSpecificClass(),
                                                        mandatory(annotation));
            }
            return dependency;
        }
        );
    }

    private static boolean mandatory(Resolved annotation) {
        return annotation.mandatory();
    }

    private static Object identifier(Resolved annotation) {
        if (!Objects.equals("", annotation.identifier().trim())) {
            return annotation.identifier();
        }
        return null;
    }

    public static <TargetT extends DependencyAware> Collection<DependencyAware> parseAnnotations(
            TargetT targetObject) {
        return allFields(targetObject.getClass())
                .stream()
                .filter(AnnotatedDependencyParser::isParameterAnnotated)
                .map(field -> asDependencyAware(field, targetObject))
                .collect(Collectors.toList());
    }

    private static List<Field> allFields(Class type) {
        if (type == Object.class) {
            return Collections.emptyList();
        }

        return Stream.concat(Stream.of(type.getDeclaredFields()),
                             allFields(type.getSuperclass()).stream())
                     .collect(Collectors.toList());
    }


    private static class AnnotatedDependencyAware implements DependencyAware {
        private final ThrowingFunction<DependencyContext, Object, DependencyException> resolver;
        private FieldAnalysis fieldAnalysis;

        private AnnotatedDependencyAware(FieldAnalysis fieldAnalysis,
                                         ThrowingFunction<DependencyContext, Object, DependencyException> resolver) {
            this.fieldAnalysis = fieldAnalysis;
            this.resolver = resolver;
        }

        @Override
        public void resolve(DependencyContext context) throws DependencyException {
            try {
                fieldAnalysis.assign(resolver.apply(context));
            } catch (IllegalAccessException e) {
                throw new DependencyException(
                        "Could not assign dependency from annotation because: " + e.getMessage());
            }
        }
    }
}
