package io.aboutcode.stage.configuration;

import java.util.List;

/**
 * A {@link ConfigurationParameter} that permits only one value to be present.
 */
public abstract class SingleValueConfigurationParameter extends ConfigurationParameter {
    /**
     * See {@link ConfigurationParameter#ConfigurationParameter(String, String, boolean, String)}
     *
     * @param name        The name of the parameter, which has to be used in the argument that
     *                    matches this parameter
     * @param description The (human readable) description of what this parameter is used for
     * @param mandatory   If true, the application will terminate if the argument for this
     *                    parameter
     * @param typeName    The type of the expected value, e.g. "Float", "Integer", etc.
     */
    @SuppressWarnings("WeakerAccess")
    public SingleValueConfigurationParameter(String name, String description, boolean mandatory,
                                             String typeName) {
        super(name, description, mandatory, typeName);
    }

    private static String expectOne(String name, List<String> values) {
        if (values == null || values.size() == 0) {
            return null;
        }

        if (values.size() > 1) {
            throw new IllegalArgumentException(
                    String.format("Too many values for parameter '%s': %s",
                                  name,
                                  String.join(", ", values)));
        }

        return values.iterator().next();
    }

    /**
     * Invokes {@link SingleValueConfigurationParameter#apply(boolean, String)} with the only
     * argument specified for this parameter.
     *
     * @param isParameterPresent True if the parameter has been set, false otherwise
     * @param values             The values to parse and apply
     *
     * @throws IllegalArgumentException If more than one argument has been provided
     */
    @Override
    public final void apply(boolean isParameterPresent, List<String> values)
            throws IllegalArgumentException {
        apply(isParameterPresent, expectOne(getName(), values));
    }

    /**
     * Parses and applies the specified value for this parameter.
     *
     * @param isParameterPresent True if the parameter has been set, false otherwise
     * @param value              The value to parse and apply
     *
     * @throws IllegalArgumentException Thrown if the parameter is not in line with what is expected
     *                                  from the parser
     */
    protected abstract void apply(boolean isParameterPresent, String value)
            throws IllegalArgumentException;
}
