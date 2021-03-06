package io.aboutcode.stage.application;

import io.aboutcode.stage.configuration.ApplicationConfigurationContext;

/**
 * Base interface that needs to be implemented by an application so that it can be managed by an
 * {@link ApplicationContainer}
 */
public interface Application {
    /**
     * Configure the application by adding command line parameters and component bundle.
     *
     * @param context The context to use for configuring the application
     */
    default void configure(ApplicationConfigurationContext context) {

    }

    /**
     * Assemble the application by adding components
     *
     * @param context The context to use for assembling the application
     */
    default void assemble(ApplicationAssemblyContext context) {

    }
}
