package io.aboutcode.stage.feature;

import io.aboutcode.stage.configuration.ConfigurationContext;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * An addon for an {@link io.aboutcode.stage.application.Application} that can tie into the runtime
 * phases of the Application and add or extends functionality.
 */
// TODO: allow the feature to access more phases
public interface Feature {
    /**
     * <p>Allows the feature to add configuration parameters to the application. With these
     * parameters, the feature can be configured before the actual resolution of application
     * arguments begins.</p>
     *
     * <p>Note that the configuration parameters added through this method follow the same
     * rules as conventional configuration parameters.</p>
     *
     * @param configurationContext The context to add configuration parameters through
     */
    void configure(ConfigurationContext configurationContext);

    /**
     * <p>Allows the feature to influence the application arguments before they are passed on to
     * the corresponding {@link io.aboutcode.stage.component.Component}s and/or {@link
     * io.aboutcode.stage.component.ComponentBundle}s.</p>
     *
     * <p>Note that if this is called for each feature added to the {@link
     * io.aboutcode.stage.application.Application} in random order.</p>
     *
     * @param applicationArguments The application arguments that will until now be used to
     *                             configure the application. This map will not be modifiable
     *
     * @return The complete configuration map that should be used to configure the application and
     * each subsequent feature
     */
    Map<String, Supplier<List<String>>> processApplicationArguments(
            Map<String, Supplier<List<String>>> applicationArguments);
}
