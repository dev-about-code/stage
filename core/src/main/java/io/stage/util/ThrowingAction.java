package io.aboutcode.stage.util;

/**
 * A consumer that does not take any argument and can throw an exception.
 * <p>
 * <em>Note that the default implementation throws a {@link RuntimeException} because it cannot
 * throw a caught exception.</em>
 */
public interface ThrowingAction<ExceptionT extends Exception> extends Action {
   /**
    * Performs this operation
    */
   void tryAccept() throws ExceptionT;

   @Override
   default void accept() {
      try {
         tryAccept();
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }
}
