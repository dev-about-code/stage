package io.aboutcode.stage.configuration;

import io.aboutcode.stage.component.ComponentBundle;

/**
 * Context for configuring an application prior to assembly. This allows specifying configuration
 * parameters for the components of the application.
 */
public interface ApplicationConfigurationContext {
    /**
     * Add a {@link ComponentBundle} to the application that resolves into configuration parameters
     * and components.
     *
     * @param componentBundle The component bundle to add
     */
    void addComponentBundle(ComponentBundle componentBundle);

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
