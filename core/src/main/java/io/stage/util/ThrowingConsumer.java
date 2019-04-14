package io.aboutcode.stage.util;

import java.util.function.Consumer;

/**
 * A consumer that can also throw an exception.
 */
public interface ThrowingConsumer<R, ExceptionT extends Throwable> extends Consumer<R> {
   /**
    * Performs this operation on the given argument.
    *
    * @param r the input argument
    */
   void acceptThrowing(R r) throws ExceptionT;

   @Override
   default void accept(R r) {
      try {
         acceptThrowing(r);
      } catch (Throwable exceptionT) {
         throw new RuntimeException(exceptionT);
      }
   }
}
