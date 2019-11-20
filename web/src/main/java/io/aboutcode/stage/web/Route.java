package io.aboutcode.stage.web;

import io.aboutcode.stage.web.request.RequestHandler;
import io.aboutcode.stage.web.request.RequestType;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>A route that can be exposed through a web server.</p>
 * <p>This class separates the logical definition of Routes from the actual implementation so that
 * the implementation can be changed without having to change the client code.</p>
 */
public final class Route {
    private final String path;
    private final RequestHandler requestHandler;
    private final RequestType type;

    private Route(RequestType type, String path, RequestHandler requestHandler) {
        this.type = type;
        this.path = path;
        this.requestHandler = requestHandler;
    }

    /**
     * Convenience method to create a list of the specified {@link Route}s.
     *
     * @param routes The routes to create a list for
     *
     * @return The created list, never null
     */
    public static List<Route> list(Route... routes) {
        return Stream.of(routes).collect(Collectors.toList());
    }

    /**
     * A route that is invoked before regular routes are matched if the specified path matches.
     *
     * @param path           The path for which to invoke the request handler
     * @param requestHandler The requeshandler to invoke
     *
     * @return The created route
     */
    public static Route before(String path, RequestHandler requestHandler) {
        return new Route(RequestType.BEFORE_ALL, path, requestHandler);
    }

    /**
     * A route that is always invoked before regular routes are matched.
     *
     * @param requestHandler The requeshandler to invoke
     *
     * @return The created route
     */
    public static Route before(RequestHandler requestHandler) {
        return new Route(RequestType.BEFORE_ALL, null, requestHandler);
    }

    /**
     * A route that is invoked after regular routes are matched if the specified path matches.
     *
     * @param path           The path for which to invoke the request handler
     * @param requestHandler The requeshandler to invoke
     *
     * @return The created route
     */
    public static Route after(String path, RequestHandler requestHandler) {
        return new Route(RequestType.AFTER_ALL, path, requestHandler);
    }

    /**
     * A route that is always invoked after regular routes are matched.
     *
     * @param requestHandler The requeshandler to invoke
     *
     * @return The created route
     */
    public static Route after(RequestHandler requestHandler) {
        return new Route(RequestType.AFTER_ALL, null, requestHandler);
    }

    /**
     * A route that is invoked for a GET request on the specified path.
     *
     * @param path           The path for which to invoke the request handler
     * @param requestHandler The requeshandler to invoke
     *
     * @return The created route
     */
    public static Route get(String path, RequestHandler requestHandler) {
        return new Route(RequestType.GET, path, requestHandler);
    }

    /**
     * A route that is invoked for a POST request on the specified path.
     *
     * @param path           The path for which to invoke the request handler
     * @param requestHandler The requeshandler to invoke
     *
     * @return The created route
     */
    public static Route post(String path, RequestHandler requestHandler) {
        return new Route(RequestType.POST, path, requestHandler);
    }

    /**
     * A route that is invoked for a PUT request on the specified path.
     *
     * @param path           The path for which to invoke the request handler
     * @param requestHandler The requeshandler to invoke
     *
     * @return The created route
     */
    public static Route put(String path, RequestHandler requestHandler) {
        return new Route(RequestType.PUT, path, requestHandler);
    }

    /**
     * A route that is invoked for a DELETE request on the specified path.
     *
     * @param path           The path for which to invoke the request handler
     * @param requestHandler The requeshandler to invoke
     *
     * @return The created route
     */
    public static Route delete(String path, RequestHandler requestHandler) {
        return new Route(RequestType.DELETE, path, requestHandler);
    }

    /**
     * A route that is invoked for a OPTIONS request on the specified path.
     *
     * @param path           The path for which to invoke the request handler
     * @param requestHandler The requeshandler to invoke
     *
     * @return The created route
     */
    public static Route options(String path, RequestHandler requestHandler) {
        return new Route(RequestType.OPTIONS, path, requestHandler);
    }

    /**
     * A route that is invoked for a PATCH request on the specified path.
     *
     * @param path           The path for which to invoke the request handler
     * @param requestHandler The requeshandler to invoke
     *
     * @return The created route
     */
    public static Route patch(String path, RequestHandler requestHandler) {
        return new Route(RequestType.PATCH, path, requestHandler);
    }

    /**
     * Returns the type of this route.
     *
     * @return The type of this route
     */
    public RequestType getType() {
        return type;
    }

    /**
     * Returns the path of this route.
     *
     * @return The path of this route
     */
    public String getPath() {
        return path;
    }

    /**
     * Returns the request handler of this route.
     *
     * @return The request handler of this route
     */
    public RequestHandler getRequestHandler() {
        return requestHandler;
    }
}
