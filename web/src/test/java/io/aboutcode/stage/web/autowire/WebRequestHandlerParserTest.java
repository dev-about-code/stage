package io.aboutcode.stage.web.autowire;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.gson.annotations.SerializedName;
import io.aboutcode.stage.web.autowire.auth.AuthorizationRealm;
import io.aboutcode.stage.web.autowire.auth.PermissiveAuthorizationRealm;
import io.aboutcode.stage.web.autowire.exception.AutowiringException;
import io.aboutcode.stage.web.Route;
import io.aboutcode.stage.web.request.Request;
import io.aboutcode.stage.web.request.RequestType;
import io.aboutcode.stage.web.response.InternalServerError;
import io.aboutcode.stage.web.response.Ok;
import io.aboutcode.stage.web.response.Response;
import io.aboutcode.stage.web.serialization.JsonWebSerialization;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Test;

public class WebRequestHandlerParserTest {
    private Request request;
    private Response currentResponse;
    private WebRequestHandlerParser parser;
    private Set<AuthorizationRealm> availableAuthorizationRealms;
    private AutowiringRequestContext context;

    private static Route get(String path, List<Route> routes) {
        return routes.stream()
                     .filter(route -> Objects.equals(route.getPath(), path))
                     .findFirst()
                     .orElseThrow(() -> new IllegalStateException(
                             "Could not find route for path: " + path));
    }

    @Before
    public void setUp() {
        AuthorizationRealm defaultAuthorizationRealm = new PermissiveAuthorizationRealm();
        availableAuthorizationRealms = Stream
                .of(defaultAuthorizationRealm, new DummyAuthorizationRealm())
                .collect(Collectors.toSet());
        context = new AutowiringRequestContext() {
            @Override
            public <T> T deserialize(String input, Class<T> type) {
                //noinspection unchecked
                return (T) input;
            }

            @Override
            public String serialize(Object input) {
                return input.toString();
            }

            @Override
            public Response serialize(Exception e) {
                return InternalServerError.with(e.getMessage());
            }
        };

        parser = new WebRequestHandlerParser(availableAuthorizationRealms, context);
        request = mock(Request.class);
        currentResponse = mock(Response.class);

        when(request.pathParam("VERSION_PATH")).thenReturn(Optional.empty());
    }

    @Test
    public void pathPlusMethodTest() throws Exception {
        TestHandler target = new TestHandler();
        List<Route> routes = parser.parse(null, set(target));
        assertEquals(7, routes.size());

        Route route = get("/test/zero", routes);
        assertEquals(RequestType.GET, route.getType());
        Response response = route.getRequestHandler().process(request, currentResponse);
        assertEquals(200, response.status());
        Object data = response.data();
        assertNotNull(data);
        assertEquals("OK", data);

        route = get("/test/one", routes);
        assertEquals(RequestType.GET, route.getType());
        response = route.getRequestHandler().process(request, currentResponse);
        assertEquals(200, response.status());
        data = response.data();
        assertNotNull(data);
        assertEquals("OK", data);

        route = get("/test/two", routes);
        assertEquals(RequestType.GET, route.getType());
        response = route.getRequestHandler().process(request, currentResponse);
        assertEquals(200, response.status());
        data = response.data();
        assertNotNull(data);
        assertEquals("two", data);

        when(request.pathParam(eq("input"))).thenReturn(Optional.of("inputValue"));

        route = get("/test/three/:input", routes);
        assertEquals(RequestType.GET, route.getType());
        response = route.getRequestHandler().process(request, currentResponse);
        assertEquals(200, response.status());
        data = response.data();
        assertNotNull(data);
        assertEquals("inputValue", data);

        route = get("/test/four/:input/more", routes);
        assertEquals(RequestType.GET, route.getType());
        response = route.getRequestHandler().process(request, currentResponse);
        assertEquals(200, response.status());
        data = response.data();
        assertNotNull(data);
        assertEquals("inputValue", data);

        when(request.pathParam(eq("input"))).thenReturn(Optional.of("123"));

        route = get("/test/five/:input", routes);
        assertEquals(RequestType.GET, route.getType());
        response = route.getRequestHandler().process(request, currentResponse);
        assertEquals(200, response.status());
        data = response.data();
        assertNotNull(data);
        assertEquals("123", data);

        when(request.queryParam(eq("input"))).thenReturn(Optional.of("query"));

        route = get("/test/six", routes);
        assertEquals(RequestType.GET, route.getType());
        response = route.getRequestHandler().process(request, currentResponse);
        assertEquals(200, response.status());
        data = response.data();
        assertNotNull(data);
        assertEquals("query", data);

    }

    @Test
    public void rootPathTest() throws Exception {
        parser = new WebRequestHandlerParser(availableAuthorizationRealms, context);

        List<Route> routes = parser.parse("root", set(new DummyHandler()));
        assertEquals(1, routes.size());

        Route route = get("/root/one", routes);
        assertEquals(RequestType.GET, route.getType());
        Response response = route.getRequestHandler().process(request, currentResponse);
        assertEquals(200, response.status());
        Object data = response.data();
        assertNotNull(data);
        assertEquals("OK", data);
    }

    @Test
    public void nonPublic() throws Exception {
        WebRequestHandler target = new WebRequestHandler() {
            @GET("/nonpublic")
            String testDefault(@QueryParameter("input") String input) {
                return input;
            }

            @GET("/nonpublic")
            protected String testProtected(@QueryParameter("input") String input) {
                return input;
            }

            @GET("/nonpublic")
            private String testPrivate(@QueryParameter("input") String input) {
                return input;
            }
        };
        List<Route> routes = parser.parse(null, set(target));
        assertEquals(0, routes.size());
    }

    @Test
    public void post() throws Exception {
        final String path = "post";
        final String result = "data";
        WebRequestHandler target = new WebRequestHandler() {
            @POST(path)
            public String test(@QueryParameter("input") String input) {
                return input;
            }
        };
        List<Route> routes = parser.parse(null, set(target));
        assertEquals(1, routes.size());

        when(request.queryParam(eq("input"))).thenReturn(Optional.of(result));

        Route route = get("/" + path, routes);
        assertEquals(RequestType.POST, route.getType());
        Response response = route.getRequestHandler().process(request, currentResponse);
        assertEquals(200, response.status());
        Object data = response.data();
        assertNotNull(data);
        assertEquals(result, data);
    }

    @Test
    public void patch() throws Exception {
        final String path = "patch";
        final String result = "data";
        WebRequestHandler target = new WebRequestHandler() {
            @PATCH(path)
            public String test(@QueryParameter("input") String input) {
                return input;
            }
        };
        List<Route> routes = parser.parse(null, set(target));
        assertEquals(1, routes.size());

        when(request.queryParam(eq("input"))).thenReturn(Optional.of(result));

        Route route = get("/" + path, routes);
        assertEquals(RequestType.PATCH, route.getType());
        Response response = route.getRequestHandler().process(request, currentResponse);
        assertEquals(200, response.status());
        Object data = response.data();
        assertNotNull(data);
        assertEquals(result, data);
    }

    @Test
    public void options() throws Exception {
        final String path = "options";
        final String result = "data";
        WebRequestHandler target = new WebRequestHandler() {
            @OPTIONS(path)
            public String test(@QueryParameter("input") String input) {
                return input;
            }
        };
        List<Route> routes = parser.parse(null, set(target));
        assertEquals(1, routes.size());

        when(request.queryParam(eq("input"))).thenReturn(Optional.of(result));

        Route route = get("/" + path, routes);
        assertEquals(RequestType.OPTIONS, route.getType());
        Response response = route.getRequestHandler().process(request, currentResponse);
        assertEquals(200, response.status());
        Object data = response.data();
        assertNotNull(data);
        assertEquals(result, data);
    }

    @Test
    public void put() throws Exception {
        final String path = "put";
        final String result = "data";
        WebRequestHandler target = new WebRequestHandler() {
            @PUT(path)
            public String test(@QueryParameter("input") String input) {
                return input;
            }
        };
        List<Route> routes = parser.parse(null, set(target));
        assertEquals(1, routes.size());

        when(request.queryParam(eq("input"))).thenReturn(Optional.of(result));

        Route route = get("/" + path, routes);
        assertEquals(RequestType.PUT, route.getType());
        Response response = route.getRequestHandler().process(request, currentResponse);
        assertEquals(200, response.status());
        Object data = response.data();
        assertNotNull(data);
        assertEquals(result, data);
    }

    @Test
    public void delete() throws Exception {
        final String path = "delete";
        final String result = "data";
        WebRequestHandler target = new WebRequestHandler() {
            @DELETE(path)
            public String test(@QueryParameter("input") String input) {
                return input;
            }
        };
        List<Route> routes = parser.parse(null, set(target));
        assertEquals(1, routes.size());

        when(request.queryParam(eq("input"))).thenReturn(Optional.of(result));

        Route route = get("/" + path, routes);
        assertEquals(RequestType.DELETE, route.getType());
        Response response = route.getRequestHandler().process(request, currentResponse);
        assertEquals(200, response.status());
        Object data = response.data();
        assertNotNull(data);
        assertEquals(result, data);
    }

    @Test(expected = AutowiringException.class)
    public void rawIllegal() throws Exception {
        WebRequestHandler target = new WebRequestHandler() {
            @GET("/raw")
            @Raw()
            public String test(@QueryParameter("input") String input) {
                return input;
            }
        };
        parser.parse(null, set(target));
    }

    @Test
    public void raw() throws Exception {
        final String path = "/raw";
        WebRequestHandler target = new WebRequestHandler() {
            @GET(path)
            @Raw
            public Response test(Request request) {
                return Ok.with(request.path());
            }
        };
        List<Route> routes = parser.parse(null, set(target));
        assertEquals(1, routes.size());

        when(request.path()).thenReturn(path);

        Route route = get(path, routes);
        assertEquals(RequestType.GET, route.getType());
        Response response = route.getRequestHandler().process(request, currentResponse);
        assertEquals(200, response.status());
        Object result = response.data();
        assertNotNull(result);
        assertEquals(path, result);
    }

    @Test
    public void body() throws Exception {
        AutowiringRequestContext context = new AutowiringRequestContext() {
            private final JsonWebSerialization jsonConverter = new JsonWebSerialization();

            @Override
            public <T> T deserialize(String input, Class<T> type) {
                return jsonConverter.deserialize(input, type);
            }

            @Override
            public String serialize(Object input) {
                return jsonConverter.serialize(input);
            }

            @Override
            public Response serialize(Exception e) {
                return InternalServerError.with(e.getMessage());
            }
        };
        TestInput input = new TestInput(
                "SUPER",
                123L,
                1.23d
        );
        when(request.body()).thenReturn(context.serialize(input));

        parser = new WebRequestHandlerParser(null, context);

        WebRequestHandler target = new WebRequestHandler() {
            @GET("/object")
            public String test(@Body TestInput input) {
                return input.stringInput;
            }
        };
        List<Route> routes = parser.parse(null, set(target));
        assertEquals(1, routes.size());

        Route route = get("/object", routes);
        assertEquals(RequestType.GET, route.getType());
        Response response = route.getRequestHandler().process(request, currentResponse);
        assertEquals(200, response.status());
        Object result = response.data();
        assertNotNull(result);
        assertEquals(context.serialize(input.stringInput), result);
    }

    private static class TestInput {
        @SerializedName("string")
        private final String stringInput;
        @SerializedName("long")
        private final long longInput;
        @SerializedName("double")
        private final double doubleInput;

        private TestInput(String stringInput, long longInput, double doubleInput) {
            this.stringInput = stringInput;
            this.longInput = longInput;
            this.doubleInput = doubleInput;
        }
    }

    private static class DummyAuthorizationRealm implements AuthorizationRealm {
        @Override
        public boolean isAuthorized(Request request) {
            return true;
        }
    }

    @Path("/api")
    private abstract static class Parent {
        @GET("zero")
        public void zero() {

        }

        @GET("parentone")
        public void one() {

        }
    }

    @Path("/test")
    private static class TestHandler extends Parent implements WebRequestHandler {
        @GET("one")
        public void one() {

        }

        @GET("two")
        public String two() {
            return "two";
        }

        @GET("three/:input")
        public String three(@PathParameter("input") String input) {
            return input;
        }

        @GET("four/:input/more")
        public String four(@PathParameter("input") String input) {
            return input;
        }

        @GET("five/:input")
        public String five(@PathParameter("input") long input) {
            return Long.toString(input);
        }

        @GET("six")
        public String six(@QueryParameter("input") String input) {
            return input;
        }
    }

    private static class DummyHandler implements WebRequestHandler {
        @GET("one")
        public void one() {

        }
    }
    
    private static <T> Set<T> set(T object) {
        return Stream.of(object).collect(Collectors.toSet());
    }
}