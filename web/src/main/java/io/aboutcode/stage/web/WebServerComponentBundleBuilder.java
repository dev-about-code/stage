package io.aboutcode.stage.web;

import io.aboutcode.stage.application.ApplicationAssemblyContext;
import io.aboutcode.stage.component.ComponentBundle;
import io.aboutcode.stage.component.ComponentContainer;
import io.aboutcode.stage.configuration.ApplicationConfigurationContext;
import io.aboutcode.stage.web.autowire.AutowiringRequestContext;
import io.aboutcode.stage.web.response.InternalServerError;
import io.aboutcode.stage.web.response.Response;
import io.aboutcode.stage.web.serialization.DefaultExceptionSerialization;
import io.aboutcode.stage.web.serialization.JsonWebSerialization;
import io.aboutcode.stage.web.serialization.WebSerialization;
import io.aboutcode.stage.web.websocket.WebsocketIo;
import io.aboutcode.stage.web.websocket.standard.TypedWebsocketMessage;
import io.aboutcode.stage.web.websocket.standard.io.NotImplementedIo;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

/**
 * Component bundle for adding web server capabilities to a project.
 */
public final class WebServerComponentBundleBuilder {
    private String rootPath;
    private String prefix;
    private Object identifier;
    private boolean secure;
    private String internalStaticFolder;
    private Set<Class> validEndpoints;
    private WebSerialization serialization;
    private WebsocketIo<? extends TypedWebsocketMessage> websocketIo;
    private Function<Exception, Response> exceptionSerialization;

    private WebServerComponentBundleBuilder(String rootPath,
                                            String prefix,
                                            Object identifier,
                                            boolean secure,
                                            String internalStaticFolder,
                                            Set<Class> validEndpoints,
                                            WebSerialization serialization,
                                            WebsocketIo<? extends TypedWebsocketMessage> websocketIo,
                                            Function<Exception, Response> exceptionSerialization) {
        this.rootPath = rootPath;
        this.prefix = prefix;
        this.identifier = identifier;
        this.secure = secure;
        this.internalStaticFolder = internalStaticFolder;
        this.validEndpoints = validEndpoints;
        this.serialization = serialization;
        this.websocketIo = websocketIo;
        this.exceptionSerialization = exceptionSerialization;
    }

    /**
     * Creates a new builder.
     *
     * @return A new builder
     */
    public static WebServerComponentBundleBuilder create() {
        return new WebServerComponentBundleBuilder(null,
                                                   null,
                                                   null,
                                                   false,
                                                   null,
                                                   null,
                                                   new JsonWebSerialization(),
                                                   new NotImplementedIo<>(),
                                                   new DefaultExceptionSerialization());
    }

    /**
     * Builds the component bundle as defined by this builder.
     *
     * @return The created component bundle
     */
    public ComponentBundle build() {
        AutowiringRequestContext context = new AutowiringRequestContext() {
            @Override
            public <T> T deserialize(String input, Class<T> type) {
                return serialization.deserialize(input, type);
            }

            @Override
            public String serialize(Object input) {
                return serialization.serialize(input);
            }

            @Override
            public Response serialize(Exception e) {
                try {
                    return exceptionSerialization.apply(e);
                } catch (Exception e1) {
                    return InternalServerError.with("Error mapping exception: " + e.getMessage());
                }
            }
        };

        if (secure) {
            return new SSLWebComponentBundle(rootPath, prefix,
                                             identifier,
                                             internalStaticFolder,
                                             validEndpoints,
                                             context,
                                             websocketIo);
        }

        return new DefaultWebComponentBundle(rootPath, prefix,
                                             identifier,
                                             internalStaticFolder,
                                             validEndpoints,
                                             context,
                                             websocketIo);
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
        return new WebServerComponentBundleBuilder(rootPath,
                                                   prefix,
                                                   identifier,
                                                   secure,
                                                   internalStaticFolder,
                                                   validEndpoints,
                                                   serialization,
                                                   websocketIo,
                                                   exceptionSerialization);
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
        return new WebServerComponentBundleBuilder(rootPath,
                                                   prefix,
                                                   identifier,
                                                   secure,
                                                   internalStaticFolder,
                                                   validEndpoints,
                                                   serialization,
                                                   websocketIo,
                                                   exceptionSerialization);
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
        return new WebServerComponentBundleBuilder(rootPath,
                                                   prefix,
                                                   identifier,
                                                   secure,
                                                   folder,
                                                   validEndpoints,
                                                   serialization,
                                                   websocketIo,
                                                   exceptionSerialization);
    }

    /**
     * Registers the specified path as root path for all endpoints.
     *
     * @param path The root path which will be used as base for all endpoints
     *
     * @return This for fluent interface
     */
    public WebServerComponentBundleBuilder withRootPath(String path) {
        return new WebServerComponentBundleBuilder(path,
                                                   prefix,
                                                   identifier,
                                                   secure,
                                                   internalStaticFolder,
                                                   validEndpoints,
                                                   serialization,
                                                   websocketIo,
                                                   exceptionSerialization);
    }

    /**
     * When called, the created component bundle will add a secure web server component to the
     * {@link ComponentContainer}. That component expects SSL/TLS connections (as opposed to
     * unsecured connections).
     *
     * @return This for fluent interface
     */
    public WebServerComponentBundleBuilder secure() {
        return new WebServerComponentBundleBuilder(rootPath,
                                                   prefix,
                                                   identifier,
                                                   true,
                                                   internalStaticFolder,
                                                   validEndpoints,
                                                   serialization,
                                                   websocketIo,
                                                   exceptionSerialization);
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
        return new WebServerComponentBundleBuilder(rootPath,
                                                   prefix,
                                                   identifier,
                                                   secure,
                                                   internalStaticFolder,
                                                   validClasses != null ? new HashSet<>(
                                                           Arrays.asList(validClasses)) : null,
                                                   serialization,
                                                   websocketIo,
                                                   exceptionSerialization);
    }

    /**
     * Sets the exception mapper to use for serializing exceptions raised by the endpoints.
     *
     * @param mapper The mapper of exception to {@link Response}
     *
     * @return This for fluent interface
     */
    public WebServerComponentBundleBuilder withExceptionMapper(
            Function<Exception, Response> mapper) {
        return new WebServerComponentBundleBuilder(rootPath,
                                                   prefix,
                                                   identifier,
                                                   secure,
                                                   internalStaticFolder,
                                                   validEndpoints,
                                                   serialization,
                                                   websocketIo,
                                                   mapper);
    }

    /**
     * When called, the created component bundle will accept websocket connections, using the
     * specified protocol.
     *
     * @param websocketIo The protocol to use for websocket communication
     *
     * @return This for fluent interface
     */
    public WebServerComponentBundleBuilder withWebsocket(
            WebsocketIo<? extends TypedWebsocketMessage> websocketIo) {
        return new WebServerComponentBundleBuilder(
                rootPath,
                prefix,
                identifier,
                secure,
                internalStaticFolder,
                validEndpoints,
                serialization,
                websocketIo,
                exceptionSerialization);
    }


    /**
     * A component bundle for a secure web component
     */
    private static class SSLWebComponentBundle implements ComponentBundle {
        private final String rootPath;
        private final String prefix;
        private final Object identifier;
        private final String internalStaticFolder;
        private final Set<Class> validEndpoints;
        private AutowiringRequestContext autowiringRequestContext;
        private WebsocketIo<? extends TypedWebsocketMessage> websocketIo;

        private TslConfiguration tslConfiguration;
        private WebServerConfiguration webServerConfiguration;

        private SSLWebComponentBundle(String rootPath, String prefix,
                                      Object identifier,
                                      String internalStaticFolder,
                                      Set<Class> validEndpoints,
                                      AutowiringRequestContext autowiringRequestContext,
                                      WebsocketIo<? extends TypedWebsocketMessage> websocketIo) {
            this.rootPath = rootPath;
            this.prefix = prefix;
            this.identifier = identifier;
            this.internalStaticFolder = internalStaticFolder;
            this.validEndpoints = validEndpoints;
            this.autowiringRequestContext = autowiringRequestContext;
            this.websocketIo = websocketIo;
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
                            rootPath,
                            webServerConfiguration.getPort(),
                            webServerConfiguration.getExternalStaticFolder() == null
                            ? internalStaticFolder
                            : webServerConfiguration
                                    .getExternalStaticFolder(),
                            webServerConfiguration.getExternalStaticFolder() != null,
                            autowiringRequestContext,
                            tslConfiguration,
                            validEndpoints,
                            websocketIo));
        }
    }

    /**
     * A component bundle for the default web component
     */
    private static class DefaultWebComponentBundle implements ComponentBundle {
        private final String rootPath;
        private final String prefix;
        private final Object identifier;
        private final String internalStaticFolder;
        private final Set<Class> validEndpoints;
        private AutowiringRequestContext autowiringRequestContext;
        private WebsocketIo<? extends TypedWebsocketMessage> websocketIo;

        private WebServerConfiguration webServerConfiguration;

        private DefaultWebComponentBundle(String rootPath, String prefix,
                                          Object identifier,
                                          String internalStaticFolder,
                                          Set<Class> validEndpoints,
                                          AutowiringRequestContext autowiringRequestContext,
                                          WebsocketIo<? extends TypedWebsocketMessage> websocketIo) {
            this.rootPath = rootPath;
            this.prefix = prefix;
            this.identifier = identifier;
            this.internalStaticFolder = internalStaticFolder;
            this.validEndpoints = validEndpoints;
            this.autowiringRequestContext = autowiringRequestContext;
            this.websocketIo = websocketIo;
        }

        @Override
        public void configure(ApplicationConfigurationContext context) {
            webServerConfiguration = context.addConfigurationObject(prefix,
                                                                    new WebServerConfiguration(80));
        }

        @Override
        public void assemble(ApplicationAssemblyContext context) {
            context.addComponent(identifier, new WebServerComponent(
                    rootPath,
                    webServerConfiguration.getPort(),
                    webServerConfiguration.getExternalStaticFolder() == null ? internalStaticFolder
                                                                             : webServerConfiguration
                            .getExternalStaticFolder(),
                    webServerConfiguration.getExternalStaticFolder() != null,
                    autowiringRequestContext,
                    null,
                    validEndpoints,
                    websocketIo));
        }
    }
}
