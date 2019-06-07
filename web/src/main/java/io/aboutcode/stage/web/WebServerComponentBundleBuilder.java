package io.aboutcode.stage.web;

import io.aboutcode.stage.application.ApplicationAssemblyContext;
import io.aboutcode.stage.component.ComponentBundle;
import io.aboutcode.stage.component.ComponentContainer;
import io.aboutcode.stage.configuration.ApplicationConfigurationContext;
import io.aboutcode.stage.web.web.response.renderer.ResponseRenderer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Component bundle for adding web server capabilities to a project.
 */
public final class WebServerComponentBundleBuilder {
    private String prefix;
    private Object identifier;
    private boolean secure;
    private String internalStaticFolder;
    private Set<Class> validEndpoints;
    private ResponseRenderer responseRenderer;

    private WebServerComponentBundleBuilder(String prefix,
                                            Object identifier,
                                            boolean secure,
                                            String internalStaticFolder,
                                            Set<Class> validEndpoints,
                                            ResponseRenderer responseRenderer) {
        this.prefix = prefix;
        this.identifier = identifier;
        this.secure = secure;
        this.internalStaticFolder = internalStaticFolder;
        this.validEndpoints = validEndpoints;
        this.responseRenderer = responseRenderer;
    }

    /**
     * Creates a new builder.
     *
     * @param renderer The renderer to use for this bundle
     *
     * @return A new builder
     */
    public static WebServerComponentBundleBuilder create(ResponseRenderer renderer) {
        return new WebServerComponentBundleBuilder(null,
                                                   null,
                                                   false,
                                                   null,
                                                   null,
                                                   renderer);
    }

    /**
     * Builds the component bundle as defined by this builder.
     *
     * @return The created component bundle
     */
    public ComponentBundle build() {
        if (secure) {
            return new SSLWebComponentBundle(prefix,
                                             identifier,
                                             internalStaticFolder,
                                             validEndpoints, responseRenderer);
        }

        return new DefaultWebComponentBundle(prefix,
                                             identifier,
                                             internalStaticFolder,
                                             validEndpoints, responseRenderer);
    }

    /**
     * Adds a prefix to this builder. The prefix will be preprended to all arguments that the
     * component builder expects.
     *
     * @param prefix The prefix to use
     *
     * @return This for fluent interface
     */
    public WebServerComponentBundleBuilder withPrefix(String prefix) {
        return new WebServerComponentBundleBuilder(prefix,
                                                   identifier,
                                                   secure,
                                                   internalStaticFolder, validEndpoints,
                                                   responseRenderer);
    }

    /**
     * Adds a component identifier to this builder. The identifier will be used when adding the web
     * server component to the {@link ComponentContainer}.
     *
     * @param identifier The identifier to use
     *
     * @return This for fluent interface
     */
    public WebServerComponentBundleBuilder withIdentifier(Object identifier) {
        return new WebServerComponentBundleBuilder(prefix,
                                                   identifier,
                                                   secure,
                                                   internalStaticFolder, validEndpoints,
                                                   responseRenderer);
    }

    /**
     * Registers the specified folder as the source for static files. The folder is considered
     * internal to the classpath that this class resides in.
     *
     * @param folder The full path to the folder on this class' classpath that contains the static
     *               resources to serve
     *
     * @return This for fluent interface
     */
    public WebServerComponentBundleBuilder withInternalStaticFolder(String folder) {
        return new WebServerComponentBundleBuilder(prefix,
                                                   identifier,
                                                   secure,
                                                   folder,
                                                   validEndpoints, responseRenderer);
    }

    /**
     * When called, the created component bundle will add a secure web server component to the
     * {@link ComponentContainer}. That component expects SSL/TLS connections (as opposed to
     * unsecured connections).
     *
     * @return This for fluent interface
     */
    public WebServerComponentBundleBuilder secure() {
        return new WebServerComponentBundleBuilder(prefix,
                                                   identifier,
                                                   true,
                                                   internalStaticFolder,
                                                   validEndpoints, responseRenderer);
    }

    /**
     * When called, the created component bundle will create a web server that only processes
     * endpoints of the specified types. This is useful when adding multiple web servers to an
     * application.
     *
     * @param validClasses The classes that are valid for the web server and should be processed
     *
     * @return This for fluent interface
     */
    public WebServerComponentBundleBuilder withValidEndpointClasses(Class... validClasses) {
        return new WebServerComponentBundleBuilder(prefix,
                                                   identifier,
                                                   secure,
                                                   internalStaticFolder,
                                                   validClasses != null ? new HashSet<>(
                                                           Arrays.asList(validClasses)) : null,
                                                   responseRenderer);
    }


    /**
     * A component bundle for a secure web component
     */
    private static class SSLWebComponentBundle implements ComponentBundle {

        private final String prefix;
        private final Object identifier;
        private final String internalStaticFolder;
        private final Set<Class> validEndpoints;
        private ResponseRenderer responseRenderer;

        private TslConfiguration tslConfiguration;
        private WebServerConfiguration webServerConfiguration;

        private SSLWebComponentBundle(String prefix,
                                      Object identifier,
                                      String internalStaticFolder,
                                      Set<Class> validEndpoints,
                                      ResponseRenderer responseRenderer) {
            this.prefix = prefix;
            this.identifier = identifier;
            this.internalStaticFolder = internalStaticFolder;
            this.validEndpoints = validEndpoints;
            this.responseRenderer = responseRenderer;
        }

        @Override
        public void configure(ApplicationConfigurationContext context) {
            tslConfiguration = context.addConfigurationObject(prefix, new TslConfiguration());
            webServerConfiguration = context.addConfigurationObject(prefix,
                                                                    new WebServerConfiguration(
                                                                            443));
        }

        @Override
        public void assemble(ApplicationAssemblyContext context) {
            context.addComponent(
                    identifier,
                    new WebServerComponent(
                            webServerConfiguration.getPort(),
                            webServerConfiguration.getExternalStaticFolder() == null
                            ? internalStaticFolder
                            : webServerConfiguration
                                    .getExternalStaticFolder(),
                            webServerConfiguration.getExternalStaticFolder() != null,
                            tslConfiguration,
                            validEndpoints,
                            responseRenderer
                    ));
        }
    }

    /**
     * A component bundle for the default web component
     */
    private static class DefaultWebComponentBundle implements ComponentBundle {
        private final String prefix;
        private final Object identifier;
        private final String internalStaticFolder;
        private final Set<Class> validEndpoints;
        private ResponseRenderer responseRenderer;

        private WebServerConfiguration webServerConfiguration;

        private DefaultWebComponentBundle(String prefix,
                                          Object identifier,
                                          String internalStaticFolder,
                                          Set<Class> validEndpoints,
                                          ResponseRenderer responseRenderer) {
            this.prefix = prefix;
            this.identifier = identifier;
            this.internalStaticFolder = internalStaticFolder;
            this.validEndpoints = validEndpoints;
            this.responseRenderer = responseRenderer;
        }

        @Override
        public void configure(ApplicationConfigurationContext context) {
            webServerConfiguration = context.addConfigurationObject(prefix,
                                                                    new WebServerConfiguration(80));
        }

        @Override
        public void assemble(ApplicationAssemblyContext context) {
            context.addComponent(identifier, new WebServerComponent(
                    webServerConfiguration.getPort(),
                    webServerConfiguration.getExternalStaticFolder() == null ? internalStaticFolder
                                                                             : webServerConfiguration
                            .getExternalStaticFolder(),
                    webServerConfiguration.getExternalStaticFolder() != null,
                    null,
                    validEndpoints,
                    responseRenderer));
        }
    }
}
