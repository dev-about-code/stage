package io.aboutcode.stage.util;

import java.util.function.Function;

/**
 * A function that can also throw an exception.
 */
public interface ThrowingFunction<R, T, ExceptionT extends Throwable> extends Function<R, T> {
   /**
    * Applies this function to the given argument.
    *
    * @param r the function argument
    *
    * @return the function result
    */
   T applyThrowing(R r) throws ExceptionT;

   @Override
   default T apply(R r) {
      try {
         return applyThrowing(r);
      } catch (Throwable exceptionT) {
         throw new RuntimeException(exceptionT);
      }
   }
}
