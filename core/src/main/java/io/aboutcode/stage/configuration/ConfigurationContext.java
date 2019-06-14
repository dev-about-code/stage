package io.aboutcode.stage.configuration;

/**
 * This context is used to add configuration parameters to an {@link io.aboutcode.stage.application.Application}.
 */
public interface ConfigurationContext {
    /**
     * Adds a configuration parameter that is resolved against the specified arguments when running
     * the application.
     *
     * @param configurationParameter The configuration parameter to add to the application
     */
    void addConfigurationParameter(ConfigurationParameter configurationParameter);

    /**
     * Adds all {@link Parameter}-annotated fields of the specified configuration object with the
     * specified parameter prefix.
     *
     * @param parameterPrefix     The prefix that should be added to all configuration parameters
     *                            from the configuration object
     * @param configurationObject The object that should be analyzed for parameters
     * @param <TypeT>             The type of the configuration object
     *
     * @return The analyzed object
     */
    <TypeT> TypeT addConfigurationObject(String parameterPrefix, TypeT configurationObject);

    /**
     * Adds all {@link Parameter}-annotated fields of the specified configuration object.
     *
     * @param configurationObject The object that should be analyzed for parameters
     * @param <TypeT>             The type of the configuration object
     *
     * @return The analyzed object
     */
    <TypeT> TypeT addConfigurationObject(TypeT configurationObject);
}
