package io.aboutcode.stage.configuration;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A configuration parameter that is resolved against the arguments passed on to the application.
 * The configuration parameters must match the supplied arguments for an application to be in
 * runnable state.
 */
public abstract class ConfigurationParameter {
    private String name;
    private String description;
    private boolean mandatory;
    private String typeName;

    /**
     * @param name        The name of the parameter, which has to be used in the argument that
     *                    matches this parameter
     * @param description The (human readable) description of what this parameter is used for
     * @param mandatory   If true, the application will terminate if the argument for this parameter
     *                    is missing
     * @param typeName    The type of the expected value, e.g. "Float", "Integer", etc.
     */
    ConfigurationParameter(String name, String description, boolean mandatory, String typeName) {
        this.name = name;
        this.description = description;
        this.mandatory = mandatory;
        this.typeName = typeName;
    }

    /**
     * A configuration parameter that expects a file.
     *
     * @param name        The name of the parameter, which has to be used in the argument that
     *                    matches this parameter
     * @param description The (human readable) description of what this parameter is used for
     * @param mandatory   If true, the application will terminate if the argument for this parameter
     *                    is not present
     * @param mustExist   If true, the file must exist, otherwise the application will terminate
     * @param consumer    The consumer that accepts the parsed file argument
     *
     * @return A configuration parameter that expects a file.
     */
    public static ConfigurationParameter File(String name, String description, boolean mandatory,
                                              boolean mustExist, final Consumer<File> consumer) {
        return new SingleValueConfigurationParameter(name, description, mandatory, "File") {
            @Override
            public void apply(boolean isParameterPresent, String stringValue)
                    throws IllegalArgumentException {
                if (stringValue == null) {
                    if (mandatory) {
                        throw new IllegalArgumentException(
                                String.format("No value for parameter '%s'", name));
                    }
                } else {
                    File file = new File(stringValue);
                    if (mustExist) {
                        if (!file.exists()) {
                            throw new IllegalArgumentException(
                                    String.format("File '%s' should exists, but does not",
                                                  stringValue));
                        }
                    }
                    consumer.accept(file);
                }
            }
        };
    }

    /**
     * A configuration parameter that expects a float value.
     *
     * @param name         The name of the parameter, which has to be used in the argument that
     *                     matches this parameter
     * @param description  The (human readable) description of what this parameter is used for
     * @param mandatory    If true, the application will terminate if the argument for this
     *                     parameter is not present
     * @param defaultValue The default value for this parameter if no value is specified in the
     *                     argument
     * @param consumer     The consumer that accepts the parsed argument
     *
     * @return A configuration parameter that expects a float value.
     */
    public static ConfigurationParameter Float(String name, String description, boolean mandatory,
                                               Float defaultValue, final Consumer<Float> consumer) {
        return new SingleValueConfigurationParameter(name, description, mandatory, "Float") {
            @Override
            public void apply(boolean isParameterPresent, String stringValue) {
                Float value = defaultValue;
                if (stringValue != null) {
                    try {
                        value = Float.parseFloat(stringValue);
                    } catch (Exception e) {
                        throw new IllegalArgumentException(String.format(
                                "Error parsing float parameter '%s': %s", stringValue,
                                e.getMessage()));
                    }
                }
                consumer.accept(value);
            }
        };
    }

    /**
     * A configuration parameter that expects a double value.
     *
     * @param name         The name of the parameter, which has to be used in the argument that
     *                     matches this parameter
     * @param description  The (human readable) description of what this parameter is used for
     * @param mandatory    If true, the application will terminate if the argument for this
     *                     parameter is not present
     * @param defaultValue The default value for this parameter if no value is specified in the
     *                     argument
     * @param consumer     The consumer that accepts the parsed argument
     *
     * @return A configuration parameter that expects a double value.
     */
    public static ConfigurationParameter Double(String name, String description, boolean mandatory,
                                                Double defaultValue,
                                                final Consumer<Double> consumer) {
        return new SingleValueConfigurationParameter(name, description, mandatory, "Double") {
            @Override
            public void apply(boolean isParameterPresent, String stringValue) {
                Double value = defaultValue;
                if (stringValue != null) {
                    try {
                        value = Double.parseDouble(stringValue);
                    } catch (Exception e) {
                        throw new IllegalArgumentException(String.format(
                                "Error parsing double parameter '%s': %s", stringValue,
                                e.getMessage()));
                    }
                }
                consumer.accept(value);
            }
        };
    }

    /**
     * A configuration parameter that expects a long value.
     *
     * @param name         The name of the parameter, which has to be used in the argument that
     *                     matches this parameter
     * @param description  The (human readable) description of what this parameter is used for
     * @param mandatory    If true, the application will terminate if the argument for this
     *                     parameter is not present
     * @param defaultValue The default value for this parameter if no value is specified in the
     *                     argument
     * @param consumer     The consumer that accepts the parsed argument
     *
     * @return A configuration parameter that expects a long value.
     */
    public static ConfigurationParameter Long(String name, String description, boolean mandatory,
                                              Long defaultValue, final Consumer<Long> consumer) {
        return new SingleValueConfigurationParameter(name, description, mandatory, "Long") {
            @Override
            public void apply(boolean isParameterPresent, String stringValue) {
                Long value = defaultValue;
                if (stringValue != null) {
                    try {
                        value = Long.parseLong(stringValue);
                    } catch (Exception e) {
                        throw new IllegalArgumentException(String.format(
                                "Error parsing long parameter '%s': %s", stringValue,
                                e.getMessage()));
                    }
                }
                consumer.accept(value);
            }
        };
    }

    /**
     * A configuration parameter that expects an integer value.
     *
     * @param name         The name of the parameter, which has to be used in the argument that
     *                     matches this parameter
     * @param description  The (human readable) description of what this parameter is used for
     * @param mandatory    If true, the application will terminate if the argument for this
     *                     parameter is not present
     * @param defaultValue The default value for this parameter if no value is specified in the
     *                     argument
     * @param consumer     The consumer that accepts the parsed argument
     *
     * @return A configuration parameter that expects an integer value.
     */
    public static ConfigurationParameter Integer(String name, String description, boolean mandatory,
                                                 Integer defaultValue,
                                                 final Consumer<Integer> consumer) {
        return new SingleValueConfigurationParameter(name, description, mandatory, "Integer") {
            @Override
            public void apply(boolean isParameterPresent, String stringValue) {
                Integer value = defaultValue;
                if (stringValue != null) {
                    try {
                        value = Integer.parseInt(stringValue);
                    } catch (Exception e) {
                        throw new IllegalArgumentException(String.format(
                                "Error parsing integer parameter '%s': %s", stringValue,
                                e.getMessage()));
                    }
                }
                consumer.accept(value);
            }
        };
    }

    /**
     * A configuration parameter that expects a string value.
     *
     * @param name         The name of the parameter, which has to be used in the argument that
     *                     matches this parameter
     * @param description  The (human readable) description of what this parameter is used for
     * @param mandatory    If true, the application will terminate if the argument for this
     *                     parameter is not present
     * @param defaultValue The default value for this parameter if no value is specified in the
     *                     argument
     * @param consumer     The consumer that accepts the parsed argument
     *
     * @return A configuration parameter that expects a string value.
     */
    public static ConfigurationParameter String(String name, String description, boolean mandatory,
                                                String defaultValue,
                                                final Consumer<String> consumer) {
        return new SingleValueConfigurationParameter(name, description, mandatory, "String") {
            @Override
            public void apply(boolean isParameterPresent, String stringValue) {
                String value = defaultValue;
                if (stringValue != null) {
                    value = stringValue;
                }
                consumer.accept(value);
            }
        };
    }

    /**
     * A configuration parameter that expects a boolean value.
     *
     * @param name         The name of the parameter, which has to be used in the argument that
     *                     matches this parameter
     * @param description  The (human readable) description of what this parameter is used for
     * @param mandatory    If true, the application will terminate if the argument for this
     *                     parameter is not present
     * @param defaultValue The default value for this parameter if no value is specified in the
     *                     argument
     * @param consumer     The consumer that accepts the parsed argument
     *
     * @return A configuration parameter that expects a boolean value.
     */
    public static ConfigurationParameter Boolean(String name, String description, boolean mandatory,
                                                 Boolean defaultValue,
                                                 final Consumer<Boolean> consumer) {
        return new SingleValueConfigurationParameter(name, description, mandatory, "Boolean") {
            @Override
            public void apply(boolean isParameterPresent, String stringValue) {
                boolean value = defaultValue;
                if (stringValue != null) {
                    try {
                        value = Boolean.parseBoolean(stringValue);
                    } catch (Exception e) {
                        throw new IllegalArgumentException(String.format(
                                "Error parsing boolean parameter '%s': %s", stringValue,
                                e.getMessage()));
                    }
                }
                consumer.accept(value);
            }
        };
    }

    /**
     * A configuration parameter that results in a true value if the parameter is specified and in a
     * false value otherwise.
     *
     * @param name        The name of the parameter, which has to be used in the argument that
     *                    matches this parameter
     * @param description The (human readable) description of what this parameter is used for
     * @param consumer    The consumer that accepts the parsed argument
     *
     * @return A "switch" configuration parameter
     */
    public static ConfigurationParameter Option(String name, String description,
                                                final Consumer<Boolean> consumer) {
        return new ConfigurationParameter(name, description, false, "Option") {
            @Override
            public void apply(boolean isParameterPresent, List<String> values) {
                consumer.accept(isParameterPresent);
            }
        };
    }

    /**
     * Parses and applies the values specified for this parameter.
     *
     * @param isParameterPresent True if the parameter has been set, false otherwise
     * @param values             The values to parse and apply
     *
     * @throws IllegalArgumentException Thrown if the parameters are not in line with what is
     *                                  expected from the parser
     */
    public abstract void apply(boolean isParameterPresent, List<String> values)
            throws IllegalArgumentException;

    /**
     * Returns the name of the parameter.
     *
     * @return The (short) name of the parameter
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the full description of the parameter.
     *
     * @return The full description of the parameter
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the name of the type for logging reasons.
     *
     * @return The name of the expected type of this parameter
     */
    public final String getTypeName() {
        return typeName;
    }

    /**
     * True if the parameter is mandatory, false otherwise
     *
     * @return True if the parameter is mandatory, false otherwise
     */
    public boolean isMandatory() {
        return mandatory;
    }

    @Override
    public String toString() {
        return "ConfigurationParameter{" +
               "name='" + name + '\'' +
               ", description='" + description + '\'' +
               ", mandatory=" + mandatory +
               ", typeName='" + typeName + '\'' +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ConfigurationParameter that = (ConfigurationParameter) o;

        if (mandatory != that.mandatory) {
            return false;
        }
        if (!Objects.equals(name, that.name)) {
            return false;
        }
        return Objects.equals(description, that.description);

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (mandatory ? 1 : 0);
        return result;
    }
}
