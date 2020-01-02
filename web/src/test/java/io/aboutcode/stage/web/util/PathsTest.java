package io.aboutcode.stage.web.util;

import static org.junit.Assert.*;

import java.util.Optional;
import org.junit.Test;

public class PathsTest {
    @Test
    public void testNullPath() {
        @SuppressWarnings("ConfusingArgumentToVarargsMethod")
        Optional<String> result = Paths.concat(null);
        assertFalse(result.isPresent());
    }

    @Test
    public void testEmptyPath() {
        Optional<String> result = Paths.concat("");
        assertTrue(result.isPresent());
        assertEquals("/", result.get());
    }

    @Test
    public void testEmptyPaths() {
        Optional<String> result = Paths.concat("", "", "");
        assertTrue(result.isPresent());
        assertEquals("/", result.get());
    }

    @Test
    public void testSomeEmptyPaths() {
        Optional<String> result = Paths.concat("first", "", "last");
        assertTrue(result.isPresent());
        assertEquals("/first/last", result.get());
    }

    @Test
    public void testLeadingEmptyPaths() {
        Optional<String> result = Paths.concat("", "first", "last");
        assertTrue(result.isPresent());
        assertEquals("/first/last", result.get());
    }

    @Test
    public void testLeadingAndTrailingEmptyPaths() {
        Optional<String> result = Paths.concat("", "first", "last");
        assertTrue(result.isPresent());
        assertEquals("/first/last", result.get());
    }

    @Test
    public void testNullPaths() {
        Optional<String> result = Paths.concat("first", null, "last");
        assertTrue(result.isPresent());
        assertEquals("/first/last", result.get());
    }

    @Test
    public void testSanitizing() {
        Optional<String> result = Paths.concat("//first/last//");
        assertTrue(result.isPresent());
        assertEquals("/first/last", result.get());
    }

    @Test
    public void testDoubleSeparators() {
        Optional<String> result = Paths.concat("//first//second/third//last//");
        assertTrue(result.isPresent());
        assertEquals("/first//second/third//last", result.get());
    }
}