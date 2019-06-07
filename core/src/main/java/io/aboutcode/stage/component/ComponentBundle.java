package io.aboutcode.stage.component;

import io.aboutcode.stage.application.ApplicationAssemblyContext;
import io.aboutcode.stage.configuration.ApplicationConfigurationContext;

/**
 * <p>A component bundle allows grouping of components into reusable collections of components,
 * along with any needed configuration parameters and/or procedures.</p>
 *
 * <p><em>Note</em> that avcomponent bundle can in turn be composed of other component bundles.</p>
 */
public interface ComponentBundle {
    /**
     * Configure the component bundle by adding command line parameters or other component bundles
     * to it.
     *
     * @param context The context to configure the application through
     */
    default void configure(ApplicationConfigurationContext context) {

    }

    /**
     * Assemble the component bundle by adding components to its context, which in turn will be
     * added to the {@link ComponentContainer}.
     *
     * @param context The context to assemble the application through
     */
    default void assemble(ApplicationAssemblyContext context) {

    }
}
