package io.aboutcode.stage.dispatch;

import static org.junit.Assert.assertEquals;

import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.Test;

public class ProducingDispatcherTest {

    @Test(expected = NoSuchElementException.class)
    public void empty() {
        ProducingDispatcher<Long, Long> dispatcher = ProducingDispatcher.empty();
        assertEquals(Optional.empty(), dispatcher.dispatch(1L));
    }

    @Test
    public void valid() {
        ProducingDispatcher<Long, Long> dispatcher = ProducingDispatcher.of(1L, () -> 100L);
        assertEquals(Optional.of(100L), dispatcher.dispatch(1L));
    }

    @Test(expected = NoSuchElementException.class)
    public void nullResult() {
        ProducingDispatcher<Long, Long> dispatcher = ProducingDispatcher.of(1L, null);
        assertEquals(Optional.empty(), dispatcher.dispatch(1L));
    }
}