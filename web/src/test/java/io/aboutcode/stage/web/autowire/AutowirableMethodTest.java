package io.aboutcode.stage.web.autowire;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.aboutcode.stage.web.autowire.auth.AuthorizationRealm;
import io.aboutcode.stage.web.autowire.auth.Authorized;
import io.aboutcode.stage.web.autowire.auth.PermissiveAuthorizationRealm;
import io.aboutcode.stage.web.autowire.auth.RestrictiveAuthorizationRealm;
import io.aboutcode.stage.web.autowire.auth.Unauthorized;
import io.aboutcode.stage.web.autowire.exception.AutowiringException;
import io.aboutcode.stage.web.autowire.exception.UnauthorizedException;
import io.aboutcode.stage.web.request.Request;
import io.aboutcode.stage.web.response.Response;
import io.aboutcode.stage.web.serialization.DefaultExceptionSerialization;
import io.aboutcode.stage.web.serialization.JsonWebSerialization;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
            private final DefaultExceptionSerialization exceptionSerialization = new DefaultExceptionSerialization();

            @Override
            public <T> T deserialize(String input, Class<T> type) {
                return jsonConverter.deserialize(input, type);
            }

            @Override
            public String serialize(Object input) {
                return jsonConverter.serialize(input);
            }

            @Override
            public void setContentType(Request request, Response response) {
                jsonConverter.setContentType(request, response);
            }

            @Override
            public Response serialize(Exception e) {
                return exceptionSerialization.apply(e);
            }
        };
        request = mock(Request.class);
        when(request.body()).thenReturn(BODY);
        when(request.pathParam(anyString())).thenReturn(Optional.of(PATH_PARAM));
        when(request.queryParam(anyString())).thenReturn(Optional.of(QUERY_PARAM));
        when(request.queryParams(anyString())).thenReturn(Collections.singletonList(QUERY_PARAM));
        when(request.path()).thenReturn(PATH);
    }

    @Test
    public void testOne() throws Exception {
        Optional<AutowirableMethod> method = autowirableMethod(new TestClass(), "one");
        assertNotNull(method);
        assertTrue(method.isPresent());
        Object response = method.get().invokeFromRequest(request, context);
        assertNull(response);
    }

    @Test
    public void testTwo() throws Exception {
        Optional<AutowirableMethod> method = autowirableMethod(new TestClass(), "two");
        assertNotNull(method);
        assertTrue(method.isPresent());
        Object response = method.get().invokeFromRequest(request, context);
        assertEquals(TEST_STRING, response);
    }

    @Test(expected = AutowiringException.class)
    public void testThree() {
        autowirableMethod(new TestClass(), "three");
    }

    @Test
    public void testFour() throws Exception {
        Optional<AutowirableMethod> method = autowirableMethod(new TestClass(), "four");
        assertNotNull(method);
        assertTrue(method.isPresent());
        Object response = method.get().invokeFromRequest(request, context);
        assertEquals(PATH, response);
    }

    @Test
    public void testFive() throws Exception {
        Optional<AutowirableMethod> method = autowirableMethod(new TestClass(), "five");
        assertNotNull(method);
        assertTrue(method.isPresent());
        Object response = method.get().invokeFromRequest(request, context);
        assertEquals(PATH_PARAM, response);
    }

    @Test
    public void testSix() throws Exception {
        Optional<AutowirableMethod> method = autowirableMethod(new TestClass(), "six");
        assertNotNull(method);
        assertTrue(method.isPresent());
        Object response = method.get().invokeFromRequest(request, context);
        assertEquals(QUERY_PARAM, response);
    }

    @Test
    public void testSeven() throws Exception {
        Optional<AutowirableMethod> method = autowirableMethod(new TestClass(), "seven");
        assertNotNull(method);
        assertTrue(method.isPresent());
        Object response = method.get().invokeFromRequest(request, context);
        assertEquals(BODY, response);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testEight() throws Exception {
        Optional<AutowirableMethod> method = autowirableMethod(new TestClass(), "eight");
        assertNotNull(method);
        assertTrue(method.isPresent());
        method.get().invokeFromRequest(request, context);
    }

    @Test
    public void testNine() throws Exception {
        when(request.queryParam(anyString())).thenReturn(Optional.empty());
        when(request.queryParams(anyString())).thenReturn(Collections.emptyList());
        Optional<AutowirableMethod> method = autowirableMethod(new TestClass(), "nine");
        assertNotNull(method);
        assertTrue(method.isPresent());
        Object response = method.get().invokeFromRequest(request, context);
        assertEquals("default", response);
    }

    @Test
    public void complexTypeTestOne() throws Exception {
        Optional<AutowirableMethod> method = autowirableMethod(new ComplexTypeTest(), "one");
        assertNotNull(method);
        assertTrue(method.isPresent());
        Object response = method.get().invokeFromRequest(request, context);
        assertEquals(PATH_PARAM, response);
    }

    @Test
    public void complexTypeTestTwo() throws Exception {
        when(request.queryParams(anyString())).thenReturn(Stream.of("a", "b", "c", "a").collect(
                Collectors.toList()));
        Optional<AutowirableMethod> method = autowirableMethod(new ComplexTypeTest(), "two");
        assertNotNull(method);
        assertTrue(method.isPresent());
        Object response = method.get().invokeFromRequest(request, context);
        assertEquals(PATH_PARAM, response);
    }

    @Test
    public void complexTypeTestThree() throws Exception {
        when(request.queryParams(anyString())).thenReturn(Stream.of("a", "b", "c", "a").collect(
                Collectors.toList()));
        Optional<AutowirableMethod> method = autowirableMethod(new ComplexTypeTest(), "three");
        assertNotNull(method);
        assertTrue(method.isPresent());
        Object response = method.get().invokeFromRequest(request, context);
        assertEquals(PATH_PARAM, response);
    }

    @Test
    public void complexTypeTestFour() throws Exception {
        when(request.queryParams(anyString())).thenReturn(Stream.of("a", "b", "c", "a").collect(
                Collectors.toList()));
        Optional<AutowirableMethod> method = autowirableMethod(new ComplexTypeTest(), "four");
        assertNotNull(method);
        assertTrue(method.isPresent());
        Object response = method.get().invokeFromRequest(request, context);
        assertEquals("a-b-c-a", response);
    }

    @Test
    public void complexTypeTestFive() throws Exception {
        when(request.queryParams(anyString())).thenReturn(Stream.of("a", "b", "c", "a").collect(
                Collectors.toList()));
        Optional<AutowirableMethod> method = autowirableMethod(new ComplexTypeTest(), "five");
        assertNotNull(method);
        assertTrue(method.isPresent());
        Object response = method.get().invokeFromRequest(request, context);
        assertEquals("a-b-c-a", response);
    }

    @Test
    public void complexTypeTestSix() throws Exception {
        when(request.queryParams(anyString())).thenReturn(Stream.of("a", "b", "c", "a").collect(
                Collectors.toList()));
        Optional<AutowirableMethod> method = autowirableMethod(new ComplexTypeTest(), "six");
        assertNotNull(method);
        assertTrue(method.isPresent());
        Object response = method.get().invokeFromRequest(request, context);
        assertEquals("a-b-c-a", response);
    }

    @Test
    public void complexTypeTestSeven() throws Exception {
        when(request.queryParams(anyString())).thenReturn(Stream.of("a", "b", "c", "a").collect(
                Collectors.toList()));
        Optional<AutowirableMethod> method = autowirableMethod(new ComplexTypeTest(), "seven");
        assertNotNull(method);
        assertTrue(method.isPresent());
        Object response = method.get().invokeFromRequest(request, context);
        assertEquals("a-b-c", response);
    }

    @Test
    public void complexTypeTestEight() throws Exception {
        when(request.queryParams(anyString())).thenReturn(Stream.of("1", "2", "1", "3").collect(
                Collectors.toList()));
        Optional<AutowirableMethod> method = autowirableMethod(new ComplexTypeTest(), "eight");
        assertNotNull(method);
        assertTrue(method.isPresent());
        Object response = method.get().invokeFromRequest(request, context);
        assertEquals("1-2-3", response);
    }

    @Test
    public void complexTypeTestNine() throws Exception {
        when(request.queryParams(anyString())).thenReturn(Stream.of("1", "2", "1", "3").collect(
                Collectors.toList()));
        Optional<AutowirableMethod> method = autowirableMethod(new ComplexTypeTest(), "nine");
        assertNotNull(method);
        assertTrue(method.isPresent());
        Object response = method.get().invokeFromRequest(request, context);
        assertEquals("1-2-1-3-", response);
    }

    @Test(expected = UnauthorizedException.class)
    public void testAuthOne() throws Exception {
        defaultAuthorizationRealm = new RestrictiveAuthorizationRealm();
        availableAuthorizationRealms.add(defaultAuthorizationRealm);

        Optional<AutowirableMethod> method = autowirableMethod(new UnauthorizedClass(), "one");
        assertNotNull(method);
        assertTrue(method.isPresent());
        method.get().invokeFromRequest(request, context);
    }

    @Test
    public void testAuthTwo() throws Exception {
        defaultAuthorizationRealm = new RestrictiveAuthorizationRealm();
        availableAuthorizationRealms.add(defaultAuthorizationRealm);

        Optional<AutowirableMethod> method = autowirableMethod(new UnauthorizedClass(), "two");
        assertNotNull(method);
        assertTrue(method.isPresent());
        Object response = method.get().invokeFromRequest(request, context);
        assertNull(response);
    }

    @Test
    public void testAuthThree() throws Exception {
        defaultAuthorizationRealm = new RestrictiveAuthorizationRealm();
        availableAuthorizationRealms.add(defaultAuthorizationRealm);

        Optional<AutowirableMethod> method = autowirableMethod(new UnauthorizedClass(), "three");
        assertNotNull(method);
        assertTrue(method.isPresent());
        Object response = method.get().invokeFromRequest(request, context);
        assertNull(response);
    }

    private Optional<AutowirableMethod> autowirableMethod(Object instance, String methodName) {
        final Map<String, Method> methodNameToMethod = Stream.of(instance.getClass().getMethods())
                                                             .collect(Collectors.toMap(
                                                                     Method::getName,
                                                                     Function.identity(),
                                                                     (current, next) -> current
                                                             ));
        return AutowirableMethod
                .from(null, instance, methodNameToMethod.get(methodName), defaultAuthorizationRealm,
                      availableAuthorizationRealms);
    }

    private static class DummyAuthorizationRealm implements AuthorizationRealm {
        @Override
        public boolean isAuthorized(Request request) {
            return true;
        }
    }

    @SuppressWarnings("unused")
    private static class TestClass {
        @GET("/")
        public void one() {

        }

        @GET("/")
        public String two() {
            return TEST_STRING;
        }

        @GET("/")
        public String three(String input) {
            return input;
        }

        @GET("/")
        public String four(Request request) {
            return request.path();
        }

        @GET("/")
        public String five(@PathParameter("input") String input) {
            return input;
        }

        @GET("/")
        public String six(@QueryParameter("input") String input) {
            return input;
        }

        @GET("/")
        public String seven(@Body String input) {
            return input;
        }

        @GET("/")
        public String eight() {
            throw new UnsupportedOperationException(ERROR);
        }

        @GET("/")
        public String nine(
                @QueryParameter(value = "input", defaultValue = "default", mandatory = false) String input) {
            return input;
        }
    }

    @SuppressWarnings("unused")
    private static class UnauthorizedClass {
        @GET("/")
        public void one() {

        }

        @GET("/")
        @Authorized(PermissiveAuthorizationRealm.class)
        public void two() {

        }

        @GET("/")
        @Unauthorized
        public void three() {

        }
    }

    @SuppressWarnings("unused")
    private static class ComplexTypeTest {
        @GET("/")
        public String one(@PathParameter("input") String[] input) {
            return String.join("-", input);
        }

        @GET("/")
        public String two(@PathParameter("input") List<String> input) {
            return String.join("-", input);
        }

        @GET("/")
        public String three(@PathParameter("input") Collection<String> input) {
            return String.join("-", input);
        }

        @GET("/")
        public String four(@QueryParameter("input") String[] input) {
            return String.join("-", input);
        }

        @GET("/")
        public String five(@QueryParameter("input") List<String> input) {
            return String.join("-", input);
        }

        @GET("/")
        public String six(@QueryParameter("input") Collection<String> input) {
            return String.join("-", input);
        }

        @GET("/")
        public String seven(@QueryParameter("input") Set<String> input) {
            return String.join("-", input);
        }

        @GET("/")
        public String eight(@QueryParameter("input") Set<Long> input) {
            return input.stream().map(el -> Long.toString(el)).collect(Collectors.joining("-"));
        }

        @GET("/")
        public String nine(@QueryParameter("input") long[] input) {
            StringBuilder builder = new StringBuilder();
            for (long el : input) {
                builder.append(el);
                builder.append("-");
            }
            return builder.toString();
        }
    }
}