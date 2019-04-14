package io.aboutcode.stage.util;

import java.util.function.Consumer;

/**
 * A consumer that does not accept an argument but merely executes an operation.
 */
public interface Action extends Consumer<Void> {
   /**
    * Performs this operation.
    */
   void accept();

   default void accept(Void ignored) {
      accept();
   }
}
