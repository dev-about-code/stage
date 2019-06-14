package io.aboutcode.stage.configuration;

import io.aboutcode.stage.component.ComponentBundle;

/**
 * Context for configuring an application prior to assembly. This allows specifying configuration
 * parameters for the components of the application.
 */
public interface ApplicationConfigurationContext extends ConfigurationContext {
    /**
     * Add a {@link ComponentBundle} to the application that resolves into configuration parameters
     * and components.
     *
     * @param componentBundle The component bundle to add
     */
    void addComponentBundle(ComponentBundle componentBundle);
}
