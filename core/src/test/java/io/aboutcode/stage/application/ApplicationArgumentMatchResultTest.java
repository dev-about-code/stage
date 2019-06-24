package io.aboutcode.stage.application;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import io.aboutcode.stage.configuration.ConfigurationParameter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;

public class ApplicationArgumentMatchResultTest {
    @Test
    public void testFullMatch() {
        List<ConfigurationParameter> configurationParameters = Stream.of(
                ConfigurationParameter.Option("test", "Test", value -> {}),
                ConfigurationParameter.String("test2", "Test", true, null, value -> {})
        ).collect(Collectors.toList());

        Map<String, Supplier<List<String>>> arguments = new HashMap<>();
        arguments.put("test", () -> Stream.of("true").collect(Collectors.toList()));
        arguments.put("test2", () -> Stream.of("Some string").collect(Collectors.toList()));

        ApplicationArgumentMatchResult result = ApplicationArgumentMatchResult
                .between(configurationParameters, arguments);

        assertTrue(result.matches());
        assertFalse(result.anyMissing());
        assertEquals(Collections.emptyList(), result.getMissing());
        assertFalse(result.anySuperfluous());
        assertEquals(Collections.emptyList(), result.getSuperfluous());
    }

    @Test
    public void testEmptyMatch() {
        ApplicationArgumentMatchResult result = ApplicationArgumentMatchResult
                .between(Collections.emptyList(), Collections.emptyMap());

        assertTrue(result.matches());
        assertFalse(result.anyMissing());
        assertEquals(Collections.emptyList(), result.getMissing());
        assertFalse(result.anySuperfluous());
        assertEquals(Collections.emptyList(), result.getSuperfluous());
    }

    @Test
    public void testMissingArguments() {
        ConfigurationParameter missing = ConfigurationParameter
                .String("test2", "Test", true, null, value -> {});
        List<ConfigurationParameter> configurationParameters = Stream.of(
                ConfigurationParameter.Option("test", "Test", value -> {}),
                missing
        ).collect(Collectors.toList());

        Map<String, Supplier<List<String>>> arguments = new HashMap<>();
        arguments.put("test", () -> Stream.of("true").collect(Collectors.toList()));

        ApplicationArgumentMatchResult result = ApplicationArgumentMatchResult
                .between(configurationParameters, arguments);

        assertFalse(result.matches());
        assertTrue(result.anyMissing());
        assertEquals(Stream.of(missing).collect(Collectors.toList()), result.getMissing());
        assertFalse(result.anySuperfluous());
        assertEquals(Collections.emptyList(), result.getSuperfluous());
    }

    @Test
    public void testSuperfluousArguments() {
        List<ConfigurationParameter> configurationParameters = Stream.of(
                ConfigurationParameter.Option("test", "Test", value -> {})
        ).collect(Collectors.toList());

        Map<String, Supplier<List<String>>> arguments = new HashMap<>();
        arguments.put("test", () -> Stream.of("true").collect(Collectors.toList()));
        arguments.put("test2", () -> Stream.of("Some string").collect(Collectors.toList()));

        ApplicationArgumentMatchResult result = ApplicationArgumentMatchResult
                .between(configurationParameters, arguments);

        assertFalse(result.matches());
        assertFalse(result.anyMissing());
        assertEquals(Collections.emptyList(), result.getMissing());
        assertTrue(result.anySuperfluous());
        assertEquals(Stream.of("test2").collect(Collectors.toList()), result.getSuperfluous());
    }

    @Test
    public void testNonMandatoryArguments() {
        List<ConfigurationParameter> configurationParameters = Stream.of(
                ConfigurationParameter.String("test", "Test", false, null, value -> {})
        ).collect(Collectors.toList());

        Map<String, Supplier<List<String>>> arguments = new HashMap<>();

        ApplicationArgumentMatchResult result = ApplicationArgumentMatchResult
                .between(configurationParameters, arguments);

        assertTrue(result.matches());
        assertFalse(result.anyMissing());
        assertEquals(Collections.emptyList(), result.getMissing());
        assertFalse(result.anySuperfluous());
        assertEquals(Collections.emptyList(), result.getSuperfluous());
    }
}