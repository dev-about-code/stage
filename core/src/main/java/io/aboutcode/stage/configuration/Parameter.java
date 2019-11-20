package io.aboutcode.stage.configuration;


import io.aboutcode.stage.util.InputConverter;
import io.aboutcode.stage.util.DefaultTypeConverters;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Objects that contain fields annotated with this can be used as a configuration object in {@link
 * ApplicationConfigurationContext#addConfigurationObject(Object)}. The fields in this annotation
 * match the fields required for {@link ConfigurationParameter}s.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Parameter {
    /**
     * The name of the parameter.
     *
     * @return The (short) name of the parameter
     */
    String name();

    /**
     * The description of the paramter
     *
     * @return The full description of the parameter
     */
    String description();

    /**
     * Specifies whether the parameter is required. Defaults to true.
     *
     * @return True if the parameter is required, false otherwise
     */
    boolean mandatory() default true;

    /**
     * The converter for the input value to the expected parameter type. By default, uses type
     * introspection to guess the correct type for a set of supported types through {@link
     * DefaultTypeConverters}
     *
     * @return The converter of the input value to the expected type of the parameter.
     */
    Class<InputConverter> inputConverter() default InputConverter.class;
}
