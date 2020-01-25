package io.aboutcode.stage.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Optional;
import org.junit.Test;

public class DefaultTypeConvertersTest {
    @Test
    public void testEnum_valid() {
        Optional<TestEnum> result = DefaultTypeConverters.getConverter(TestEnum.class)
                                                         .map(converter -> converter
                                                                 .convert("YES"));

        assertNotNull(result);
        assertTrue(result.isPresent());
        assertEquals(TestEnum.YES, result.get());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEnum_invalid() {
        DefaultTypeConverters.getConverter(TestEnum.class)
                             .map(converter -> converter.convert("MAYBE"));
    }

    private enum TestEnum {
        YES,
        NO
    }
}