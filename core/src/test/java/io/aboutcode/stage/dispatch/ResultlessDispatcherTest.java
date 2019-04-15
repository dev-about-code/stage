package io.aboutcode.stage.dispatch;

import static org.junit.Assert.assertEquals;

import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.Test;

public class ResultlessDispatcherTest {

   @Test(expected = NoSuchElementException.class)
   public void empty() {
      ResultlessDispatcher<Long> dispatcher = ResultlessDispatcher.empty();
      dispatcher.dispatch(1L);
   }

   @Test
   public void valid() {
      final AtomicLong result = new AtomicLong();
      ResultlessDispatcher<Long> dispatcher = ResultlessDispatcher.of(1L, () -> result.set(100L));
      dispatcher.dispatch(1L);
      assertEquals(100L, result.get());
   }

   @Test(expected = NullPointerException.class)
   public void nullResult() {
      ResultlessDispatcher<Long> dispatcher = ResultlessDispatcher.of(1L, null);
      dispatcher.dispatch(1L);
   }
}