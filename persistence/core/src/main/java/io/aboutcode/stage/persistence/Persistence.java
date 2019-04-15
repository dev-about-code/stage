package io.aboutcode.stage.persistence;

import java.io.IOException;

/**
 * An implementation of this interface provides means to persist state in a data store.
 */
public interface Persistence<SessionT> {

   /**
    * Performs the given action and returns its return value in a session. "Session" is defined by
    * the underlying persistence mechanism and can pertain to a transaction, a connection or any
    * similar concept.
    *
    * @param function         The function that should be executed
    * @param persistenceHints An array of hints that can be picked up by the underlying persistence
    *                         implementation and interpreted in a way that assists persistence -
    *                         note that these hints are optional and can also be ignored.
    *
    * @return The result of the execution
    *
    * @throws IOException in case of failure opening or processing the request
    */
   <ResultT, ExceptionT extends Exception> ResultT apply(
       PersistenceFunction<SessionT, ResultT, ExceptionT> function,
       PersistenceHint... persistenceHints) throws IOException, ExceptionT;


   /**
    * Executes the given action in a session."Session" is defined by the underlying persistence
    * mechanism and can pertain to a transaction, a connection or any similar concept.
    *
    * @param action           The action that should be executed
    * @param persistenceHints An array of hints that can be picked up by the underlying persistence
    *                         implementation and interpreted in a way that assists persistence -
    *                         note that these hints are optional and can also be ignored.
    *
    * @throws IOException in case of failure processing the request
    */
   <ExceptionT extends Exception> void execute(
       PersistenceAction<SessionT, ExceptionT> action,
       PersistenceHint... persistenceHints) throws IOException, ExceptionT;

   /**
    * Additional information on issued persistence functions. They can be ignored by the underlying
    * persistence implementation
    */
   enum PersistenceHint {
      /**
       * The function is only reading data, not modifying it
       */
      ReadOnly
   }

   /**
    * A persistence function that returns a value.
    *
    * @param <SessionT>   The type of session
    * @param <ResultT>    The type of result
    * @param <ExceptionT> The exception this function can throw
    */
   interface PersistenceFunction<SessionT, ResultT, ExceptionT extends Throwable> {
      ResultT execute(SessionT session) throws ExceptionT;
   }

   /**
    * A persistence action that does not return a value.
    *
    * @param <SessionT>   The type of session
    * @param <ExceptionT> The exception this function can throw
    */
   interface PersistenceAction<SessionT, ExceptionT extends Throwable> {

      void execute(SessionT session) throws ExceptionT;
   }
}
