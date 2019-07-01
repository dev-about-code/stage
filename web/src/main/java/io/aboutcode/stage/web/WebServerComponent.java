package io.aboutcode.stage.web;

import io.aboutcode.stage.component.BaseComponent;
import io.aboutcode.stage.dependency.DependencyContext;
import io.aboutcode.stage.dependency.DependencyException;
import io.aboutcode.stage.web.web.WebEndpoint;
import io.aboutcode.stage.web.web.response.renderer.ResponseRenderer;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>This implements a standard web server component that registers both websocket- and
 * http-endpoints and services them.</p>
 * <p>The component expects implementations of {@link WebEndpoint} to be present in the component
 * container. If no such components are present, the server will still start up but will not serve
 * any routes.</p>
 */
final class WebServerComponent extends BaseComponent {
    private final int port;
    private final String staticFilesFolder;
    private final boolean isStaticFolderExternal;
    private final Set<Class> validEndpoints;
    private ResponseRenderer responseRenderer;
    private TslConfiguration tslConfiguration;
    private Set<WebEndpoint> webEndpoints;

    private SparkServer sparkServer;

    /**
     * Creates a new component.
     *
     * @param port                   The port on which to listen for connections.
     * @param staticFilesFolder      The folder to expose static files from
     * @param isStaticFolderExternal If true, the static files are reloaded live
     * @param tslConfiguration       The (optional) ssl configuration to use
     * @param validEndpoints         If not null, the endpoints this server should use. All other
     * @param responseRenderer       The renderer for all responses
     */
    WebServerComponent(int port,
                       String staticFilesFolder, boolean isStaticFolderExternal,
                       TslConfiguration tslConfiguration,
                       Set<Class> validEndpoints,
                       ResponseRenderer responseRenderer) {
        this.port = port;
        this.staticFilesFolder = staticFilesFolder;
        this.isStaticFolderExternal = isStaticFolderExternal;
        this.tslConfiguration = tslConfiguration;
        this.validEndpoints = validEndpoints;
        this.responseRenderer = responseRenderer;
    }

    @Override
    public final void start() {
        // create the basic service and initialize it
        sparkServer = new SparkServer(port,
                                      tslConfiguration,
                                      staticFilesFolder,
                                      isStaticFolderExternal,
                                      webEndpoints,
                                      responseRenderer);
        sparkServer.start();
    }

    @Override
    public final void stop() {
        sparkServer.stop();
    }

    @Override
    public void resolve(DependencyContext context) throws DependencyException {
        if (validEndpoints == null) {
            webEndpoints = context.retrieveDependencies(WebEndpoint.class);
        } else {
            webEndpoints = new HashSet<>();
            for (Class validType : validEndpoints) {
                //noinspection unchecked
                webEndpoints.addAll(context.retrieveDependencies(validType));
            }
        }
    }
}
