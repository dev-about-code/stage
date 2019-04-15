package io.aboutcode.stage.dispatch;

import io.aboutcode.stage.util.Action;
import java.util.NoSuchElementException;

/**
 * Based on a regular {@link Dispatcher}, this does not produce any results when dispatching calls
 * but instead invokes the mapped action for a key.
 */
public final class ResultlessDispatcher<KeyT> {
   private final ProducingDispatcher<KeyT, Void> dispatcher;

   private ResultlessDispatcher(ProducingDispatcher<KeyT, Void> dispatcher) {
      this.dispatcher = dispatcher;
   }

   /**
    * Creates a new empty dipatcher.
    *
    * @param <KeyT> The type of the key
    *
    * @return A new dispatcher without any registered producers
    */
   public static <KeyT> ResultlessDispatcher<KeyT> empty() {
      return new ResultlessDispatcher<>(ProducingDispatcher.empty());
   }

   /**
    * Creates a new dipatcher with the specified key to {@link Action} mapping.
    *
    * @param key    The key to add
    * @param action The action to add
    * @param <KeyT> The type of the key
    *
    * @return A new dispatcher with only the specified key to producer mapping.
    */
   public static <KeyT> ResultlessDispatcher<KeyT> of(KeyT key, Action action) {
      return new ResultlessDispatcher<>(ProducingDispatcher.of(key, () -> {
         action.accept();
         return null;
      }));
   }

   /**
    * Creates a new Dispatcher from the current dispatcher and adds the specified key to {@link
    * Action} mapping to the existing mappings of this dispatcher.
    *
    * @param key    The key to add
    * @param action The action to add
    *
    * @return A new dispatcher based on this dispatcher with the specified key to {@link Action}
    * mapping added.
    */
   public ResultlessDispatcher<KeyT> with(KeyT key, Action action) {
      return new ResultlessDispatcher<>(dispatcher.with(key, () -> {
         action.accept();
         return null;
      }));
   }

   /**
    * Dispatches the call to the registered producer.
    *
    * @param key The key to dispatch for
    *
    * @throws NoSuchElementException if no {@link Action} has been registered for this key
    */
   public void dispatch(KeyT key) throws NoSuchElementException {
      dispatcher.dispatch(key);
   }
}
