package io.aboutcode.stage.configuration;

import io.aboutcode.stage.util.FieldAnalysis;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>This analyses a specified configuration object through introspection and returns a list of
 * {@link ConfigurationParameter}s extracted from its members that are annotated with {@link
 * Parameter}</p>
 */
// TODO: refactor this to completely use FieldAnalysis
public final class ParameterParser {
    private static final String TRAILING_DASHES = "-+$";

    /**
     * Returns a list of {@link ConfigurationParameter}s for the specified configuration object's
     * annotated members.
     *
     * @param parameterPrefix The prefix to use for all parameters
     * @param targetObject    The object to analyze
     * @param <ResultT>       The type of the configuration object
     *
     * @return A list of extracted {@link ConfigurationParameter}s
     */
    public static <ResultT> List<ConfigurationParameter> parseParameterClass(String parameterPrefix,
                                                                             ResultT targetObject) {
        return Stream.of(targetObject.getClass().getDeclaredFields())
                     .filter(ParameterParser::isParameterAnnotated)
                     .map(field -> asConfigurationParameter(parameterPrefix, field, targetObject))
                     .filter(Optional::isPresent)
                     .map(Optional::get)
                     .collect(Collectors.toList());
    }

    private static String getTypeName(Class type) {
        return type.getSimpleName();
    }

    private static ConfigurationParameter array(Parameter parameter,
                                                String parameterPrefix,
                                                FieldAnalysis fieldAnalysis,
                                                InputConverter inputConverter) {
        return new ArrayConfigurationParameter(
                withPrefix(parameterPrefix, parameter.name()),
                parameter.description(),
                parameter.mandatory(),
                getTypeName(fieldAnalysis.getField().getClass()),
                fieldAnalysis.getTargetObject(),
                fieldAnalysis.getSpecificClass(),
                fieldAnalysis.getField(),
                fieldAnalysis.getDefaultValue(),
                inputConverter
        );
    }

    private static ConfigurationParameter singleValue(Parameter parameter,
                                                      String parameterPrefix,
                                                      FieldAnalysis fieldAnalysis,
                                                      InputConverter inputConverter) {
        return new DefaultConfigurationParameter(
                withPrefix(parameterPrefix, parameter.name()),
                parameter.description(),
                parameter.mandatory(),
                getTypeName(fieldAnalysis.getSpecificClass()),
                fieldAnalysis.getTargetObject(),
                fieldAnalysis.getField(),
                fieldAnalysis.getDefaultValue(),
                inputConverter
        );
    }

    private static ConfigurationParameter collection(Parameter parameter,
                                                     String parameterPrefix,
                                                     FieldAnalysis fieldAnalysis,
                                                     InputConverter inputConverter) {
        return new CollectionConfigurationParameter(
                withPrefix(parameterPrefix, parameter.name()),
                parameter.description(),
                parameter.mandatory(),
                getTypeName(fieldAnalysis.getSpecificClass()),
                fieldAnalysis.getDefaultValue(),
                inputConverter,
                fieldAnalysis
        );
    }

    private static String withPrefix(String parameterPrefix, String name) {
        String result = name;
        if (Objects.nonNull(parameterPrefix) && !parameterPrefix.trim().isEmpty()) {
            result = String.format("%s-%s", parameterPrefix.replaceAll(TRAILING_DASHES, ""), name);
        }

        return result;
    }

    private static Optional<ConfigurationParameter> asConfigurationParameter(
            String parameterPrefix,
            Field field,
            Object targetObject) {
        Parameter annotation = getParameterAnnotation(field);

        FieldAnalysis fieldAnalysis = FieldAnalysis.of(field, targetObject);

        InputConverter<Object> inputConverter;
        if (annotation.inputConverter() == InputConverter.class) {
            //noinspection unchecked
            Optional<InputConverter<Object>> converter = SingleValueInputConverters
                    .getConverter(fieldAnalysis.getSpecificClass());

            // todo: unclear why orElseThrow is not working - fix
            if (converter.isPresent()) {
                inputConverter = converter.get();
            } else {
                throw new IllegalArgumentException(
                        String.format(
                                "Could not find default input converter for type '%s' of field '%s' and parameter '%s'",
                                fieldAnalysis.getSpecificClass().getSimpleName(),
                                field.getName(),
                                annotation.name())
                );
            }
        } else {
            inputConverter = instantiateFrom(annotation.inputConverter());
        }

        ConfigurationParameter result;
        switch (fieldAnalysis.getFieldType()) {
            case ARRAY:
                result = array(annotation, parameterPrefix, fieldAnalysis, inputConverter);
                break;
            case COLLECTION:
                result = collection(annotation, parameterPrefix, fieldAnalysis, inputConverter);
                break;
            case SINGLE_VALUE:
                result = singleValue(annotation, parameterPrefix, fieldAnalysis, inputConverter);
                break;
            default:
                result = null;
        }

        //noinspection ConstantConditions
        return Optional.ofNullable(result);
    }

    private static boolean isParameterAnnotated(Field field) {
        return getParameterAnnotation(field) != null;
    }

    private static Parameter getParameterAnnotation(Field field) {
        return field.getAnnotation(Parameter.class);
    }

    private static InputConverter<Object> instantiateFrom(Class<InputConverter> type) {
        try {
            //noinspection unchecked
            return (InputConverter<Object>) type.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException(
                    "Input converter does not specify a no-argument constructor");
        }
    }

    private static Object convert(InputConverter inputConverter, String value,
                                  String parameterName) {
        try {
            return inputConverter.convert(value);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    String.format(
                            "Could not convert value '%s' to target type for parameter '%s' because: %s",
                            value,
                            parameterName,
                            e.getMessage()));
        }
    }

    private static class CollectionConfigurationParameter extends ConfigurationParameter {
        private final Object defaultValue;
        private final InputConverter inputConverter;
        private final FieldAnalysis fieldAnalysis;

        private CollectionConfigurationParameter(String name,
                                                 String description,
                                                 boolean mandatory,
                                                 String typeName,
                                                 Object defaultValue,
                                                 InputConverter inputConverter,
                                                 FieldAnalysis fieldAnalysis
        ) {
            super(name, description, mandatory, typeName);
            this.defaultValue = defaultValue;
            this.inputConverter = inputConverter;
            this.fieldAnalysis = fieldAnalysis;
        }

        @Override
        public void apply(boolean isParameterPresent, List<String> values)
                throws IllegalArgumentException {
            Collection collection = (Collection) defaultValue;

            if (isParameterPresent) {
                Collection convertedValues = new ArrayList();
                for (String value : values) {
                    //noinspection unchecked
                    convertedValues.add(convert(inputConverter, value, getName()));
                }
                collection = convertedValues;
            }

            try {
                fieldAnalysis.assign(collection);
            } catch (Exception e) {
                throw new IllegalArgumentException(
                        String.format(
                                "Could not assign collection '%s' to type '%s' for parameter '%s' because: %s",
                                String.join(",", values),
                                getTypeName(),
                                getName(),
                                e.getMessage()));
            }

        }
    }

    private static class ArrayConfigurationParameter extends ConfigurationParameter {
        private final Object targetObject;
        private final Class arrayType;
        private final Field field;
        private final Object defaultValue;
        private final InputConverter inputConverter;

        private ArrayConfigurationParameter(String name,
                                            String description,
                                            boolean mandatory,
                                            String typeName,
                                            Object targetObject,
                                            Class arrayType,
                                            Field field,
                                            Object defaultValue,
                                            InputConverter inputConverter) {
            super(name, description, mandatory, typeName);
            this.targetObject = targetObject;
            this.arrayType = arrayType;
            this.field = field;
            this.defaultValue = defaultValue;
            this.inputConverter = inputConverter;
        }

        @Override
        public void apply(boolean isParameterPresent, List<String> values)
                throws IllegalArgumentException {
            Object array = defaultValue;
            if (isParameterPresent) {
                array = Array.newInstance(arrayType, values.size());
                int pos = 0;
                for (String value : values) {
                    Array.set(array, pos++, convert(inputConverter, value, getName()));
                }
            }
            try {
                field.setAccessible(true);
                field.set(targetObject, array);
            } catch (Exception e) {
                throw new IllegalArgumentException(
                        String.format(
                                "Could not assign array '%s' to type '%s' for parameter '%s' because: %s",
                                String.join(",", values),
                                getTypeName(),
                                getName(),
                                e.getMessage()));
            }
        }
    }

    private static final class DefaultConfigurationParameter extends
            SingleValueConfigurationParameter {
        private final Object targetObject;
        private final Field field;
        private final Object defaultValue;
        private final InputConverter inputConverter;

        private DefaultConfigurationParameter(String name,
                                              String description,
                                              boolean mandatory,
                                              String typeName,
                                              Object targetObject,
                                              Field field,
                                              Object defaultValue,
                                              InputConverter inputConverter) {
            super(name, description, mandatory, typeName);
            this.targetObject = targetObject;
            this.field = field;
            this.defaultValue = defaultValue;
            this.inputConverter = inputConverter;
        }

        @Override
        public void apply(boolean isParameterPresent, String value)
                throws IllegalArgumentException {
            try {
                field.set(targetObject,
                          !isParameterPresent ? defaultValue
                                              : convert(inputConverter, value, getName()));
            } catch (Exception e) {
                throw new IllegalArgumentException(
                        String.format(
                                "Could not set property '%s' of configuration for object of type '%s' because: %s",
                                field.getName(),
                                targetObject.getClass().getSimpleName(),
                                e.getMessage()));
            }
        }
    }
}
