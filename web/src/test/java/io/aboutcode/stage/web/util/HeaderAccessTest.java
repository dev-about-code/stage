package io.aboutcode.stage.web.util;

import static org.junit.Assert.assertEquals;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;

public class HeaderAccessTest {
    @Test
    public void testSimpleAcceptHeader() {
        Set<String> result = HeaderAccess.acceptHeader("text/html");
        assertEquals(1, result.size());
        assertEquals("text/html", result.iterator().next());
    }

    @Test
    public void testRegularAcceptHeader() {
        Set<String> result = HeaderAccess.acceptHeader("text/html; q=0.1");
        assertEquals(1, result.size());
        assertEquals("text/html", result.iterator().next());
    }

    @Test
    public void testRegularAcceptHeaderNonSpec() {
        Set<String> result = HeaderAccess.acceptHeader("text/html;q=0.1");
        assertEquals(1, result.size());
        assertEquals("text/html", result.iterator().next());
    }

    @Test
    public void testRegularAcceptHeaderNonSpec2() {
        Set<String> result = HeaderAccess.acceptHeader("text/html;q=0.1,");
        assertEquals(1, result.size());
        assertEquals("text/html", result.iterator().next());
    }

    @Test
    public void testRegularAcceptHeaderNonSpec3() {
        Set<String> result = HeaderAccess.acceptHeader(",text/html;q=0.1");
        assertEquals(1, result.size());
        assertEquals("text/html", result.iterator().next());
    }

    @Test
    public void testMultiAcceptHeaderNonSpec3() {
        Set<String> result = HeaderAccess.acceptHeader("text/html;q=0.1;level=1, application/*, */*");
        assertEquals(3, result.size());
        Set<String> expected = Stream.of("text/html", "application/*", "*/*").collect(Collectors.toSet());
        assertEquals(expected, result);
    }

    @Test
    public void testEmptyAcceptHeader() {
        Set<String> result = HeaderAccess.acceptHeader("");
        assertEquals(0, result.size());
    }

    @Test
    public void testNullAcceptHeader() {
        Set<String> result = HeaderAccess.acceptHeader(null);
        assertEquals(0, result.size());
    }
}