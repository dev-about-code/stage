package io.aboutcode.stage.dispatch;

import static org.junit.Assert.assertEquals;

import java.util.Optional;
import org.junit.Test;

public class DispatcherTest {

   @Test
   public void empty() {
      Dispatcher<Long, Long> dispatcher = Dispatcher.empty();
      assertEquals(Optional.empty(), dispatcher.dispatch(1L));
      assertEquals(Optional.empty(), dispatcher.dispatch(null));
   }

   @Test
   public void valid() {
      Dispatcher<Long, Long> dispatcher = Dispatcher.of(1L, 100L);
      assertEquals(Optional.of(100L), dispatcher.dispatch(1L));
      assertEquals(Optional.empty(), dispatcher.dispatch(2L));
      assertEquals(Optional.empty(), dispatcher.dispatch(null));
   }

   @Test
   public void nullResult() {
      Dispatcher<Long, Long> dispatcher = Dispatcher.of(1L, null);
      assertEquals(Optional.empty(), dispatcher.dispatch(1L));
      assertEquals(Optional.empty(), dispatcher.dispatch(2L));
      assertEquals(Optional.empty(), dispatcher.dispatch(null));
   }
}