package io.aboutcode.stage.web.autowire;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.aboutcode.stage.web.autowire.auth.AuthorizationRealm;
import io.aboutcode.stage.web.autowire.auth.Authorized;
import io.aboutcode.stage.web.autowire.auth.PermissiveAuthorizationRealm;
import io.aboutcode.stage.web.autowire.auth.RestrictiveAuthorizationRealm;
import io.aboutcode.stage.web.autowire.auth.Unauthorized;
import io.aboutcode.stage.web.autowire.exception.AutowiringException;
import io.aboutcode.stage.web.request.Request;
import io.aboutcode.stage.web.response.InternalServerError;
import io.aboutcode.stage.web.response.Response;
import io.aboutcode.stage.web.serialization.JsonWebSerialization;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Test;

public class AutowirableMethodTest {
    private static final String TEST_STRING = "TEST";
    private static final String BODY = "BODY";
    private static final String PATH_PARAM = "PATH_PARAM";
    private static final String QUERY_PARAM = "QUERY_PARAM";
    private static final String PATH = "PATH";
    private static final String ERROR = "ERROR";

    private AutowiringRequestContext context;
    private Request request;
    private AuthorizationRealm defaultAuthorizationRealm;
    private Set<AuthorizationRealm> availableAuthorizationRealms;

    @Before
    public void setUp() {
        defaultAuthorizationRealm = new PermissiveAuthorizationRealm();
        availableAuthorizationRealms = Stream
                .of(defaultAuthorizationRealm, new DummyAuthorizationRealm())
                .collect(Collectors.toSet());
        context = new AutowiringRequestContext() {
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
        request = mock(Request.class);
        when(request.body()).thenReturn(BODY);
        when(request.pathParam(anyString())).thenReturn(Optional.of(PATH_PARAM));
        when(request.queryParam(anyString())).thenReturn(Optional.of(QUERY_PARAM));
        when(request.path()).thenReturn(PATH);
    }

    @Test
    public void testOne() {
        AutowirableMethod method = autowirableMethod(new TestClass(), "one");
        assertNotNull(method);
        Response response = method.invokeFromRequest(request, context);
        assertNotNull(response);
        assertEquals(200, response.status());
        assertNotNull(response.data());
    }

    @Test
    public void testTwo() {
        AutowirableMethod method = autowirableMethod(new TestClass(), "two");
        assertNotNull(method);
        Response response = method.invokeFromRequest(request, context);
        assertNotNull(response);
        assertEquals(200, response.status());
        assertEquals(json(TEST_STRING), response.data());
    }

    @Test(expected = AutowiringException.class)
    public void testThree() {
        autowirableMethod(new TestClass(), "three");
    }

    @Test
    public void testFour() {
        AutowirableMethod method = autowirableMethod(new TestClass(), "four");
        assertNotNull(method);
        Response response = method.invokeFromRequest(request, context);
        assertNotNull(response);
        assertEquals(200, response.status());
        assertEquals(json(PATH), response.data());
    }

    @Test
    public void testFive() {
        AutowirableMethod method = autowirableMethod(new TestClass(), "five");
        assertNotNull(method);
        Response response = method.invokeFromRequest(request, context);
        assertNotNull(response);
        assertEquals(200, response.status());
        assertEquals(json(PATH_PARAM), response.data());
    }

    @Test
    public void testSix() {
        AutowirableMethod method = autowirableMethod(new TestClass(), "six");
        assertNotNull(method);
        Response response = method.invokeFromRequest(request, context);
        assertNotNull(response);
        assertEquals(200, response.status());
        assertEquals(json(QUERY_PARAM), response.data());
    }

    @Test
    public void testSeven() {
        AutowirableMethod method = autowirableMethod(new TestClass(), "seven");
        assertNotNull(method);
        Response response = method.invokeFromRequest(request, context);
        assertNotNull(response);
        assertEquals(200, response.status());
        assertEquals(json(BODY), response.data());
    }

    @Test
    public void testEight() {
        AutowirableMethod method = autowirableMethod(new TestClass(), "eight");
        assertNotNull(method);
        Response response = method.invokeFromRequest(request, context);
        assertNotNull(response);
        assertEquals(501, response.status());
        assertEquals(ERROR, response.data());
    }

    @Test
    public void testAuthOne() {
        defaultAuthorizationRealm = new RestrictiveAuthorizationRealm();
        availableAuthorizationRealms.add(defaultAuthorizationRealm);

        AutowirableMethod method = autowirableMethod(new UnauthorizedClass(), "one");
        assertNotNull(method);
        Response response = method.invokeFromRequest(request, context);
        assertNotNull(response);
        assertEquals(403, response.status());
    }

    @Test
    public void testAuthTwo() {
        defaultAuthorizationRealm = new RestrictiveAuthorizationRealm();
        availableAuthorizationRealms.add(defaultAuthorizationRealm);

        AutowirableMethod method = autowirableMethod(new UnauthorizedClass(), "two");
        assertNotNull(method);
        Response response = method.invokeFromRequest(request, context);
        assertNotNull(response);
        assertEquals(200, response.status());
    }

    @Test
    public void testAuthThree() {
        defaultAuthorizationRealm = new RestrictiveAuthorizationRealm();
        availableAuthorizationRealms.add(defaultAuthorizationRealm);

        AutowirableMethod method = autowirableMethod(new UnauthorizedClass(), "three");
        assertNotNull(method);
        Response response = method.invokeFromRequest(request, context);
        assertNotNull(response);
        assertEquals(200, response.status());
    }

    private AutowirableMethod autowirableMethod(Object instance, String methodName) {
        final Map<String, Method> methodNameToMethod = Stream.of(instance.getClass().getMethods())
                                                             .collect(Collectors.toMap(
                                                                     Method::getName,
                                                                     Function.identity(),
                                                                     (current, next) -> current
                                                             ));
        return AutowirableMethod
                .from(instance, methodNameToMethod.get(methodName), defaultAuthorizationRealm,
                      availableAuthorizationRealms);
    }

    private static class DummyAuthorizationRealm implements AuthorizationRealm {
        @Override
        public boolean isAuthorized(Request request) {
            return true;
        }
    }

    private String json(String input) {
        return context.serialize(input);
    }

    @SuppressWarnings("unused")
    private static class TestClass {

        public void one() {

        }

        public String two() {
            return TEST_STRING;
        }

        public String three(String input) {
            return input;
        }

        public String four(Request request) {
            return request.path();
        }

        public String five(@PathParameter("input") String input) {
            return input;
        }

        public String six(@QueryParameter("input") String input) {
            return input;
        }

        public String seven(@Body String input) {
            return input;
        }

        public String eight() {
            throw new UnsupportedOperationException(ERROR);
        }
    }

    @SuppressWarnings("unused")
    private static class UnauthorizedClass {
        public void one() {

        }

        @Authorized(PermissiveAuthorizationRealm.class)
        public void two() {

        }

        @Unauthorized
        public void three() {

        }
    }
}