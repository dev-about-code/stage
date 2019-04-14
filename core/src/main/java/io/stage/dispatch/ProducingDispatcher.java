package io.aboutcode.stage.dispatch;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * This {@link Dispatcher} returns the value of the registered {@link Supplier} when invoked.
 */
public final class ProducingDispatcher<KeyT, TargetT> {
   private final Dispatcher<KeyT, Supplier<TargetT>> dispatcher;

   private ProducingDispatcher(Dispatcher<KeyT, Supplier<TargetT>> dispatcher) {
      this.dispatcher = dispatcher;
   }

   /**
    * Creates a new empty dipatcher.
    *
    * @param <KeyT>    The type of the key
    * @param <TargetT> The type of the result of the producer
    *
    * @return A new dispatcher without any registered producers
    */
   public static <KeyT, TargetT> ProducingDispatcher<KeyT, TargetT> empty() {
      return new ProducingDispatcher<>(Dispatcher.empty());
   }

   /**
    * Creates a new dipatcher with the specified key to producer mapping.
    *
    * @param key            The key to add
    * @param resultProducer The producer to add
    * @param <KeyT>         The type of the key
    * @param <TargetT>      The type of the result of the producer
    *
    * @return A new dispatcher with only the specified key to producer mapping.
    */
   public static <KeyT, TargetT> ProducingDispatcher<KeyT, TargetT> of(KeyT key,
                                                                       Supplier<TargetT> resultProducer) {
      return new ProducingDispatcher<>(Dispatcher.of(key, resultProducer));
   }

   /**
    * Creates a new Dispatcher from the current dispatcher and adds the specified key to producer
    * mapping to the existing mappings of this dispatcher.
    *
    * @param key            The key to add
    * @param resultProducer The producer to add
    *
    * @return A new dispatcher based on this dispatcher with the specified key to producer mapping
    * added.
    */
   public ProducingDispatcher<KeyT, TargetT> with(KeyT key, Supplier<TargetT> resultProducer) {
      return new ProducingDispatcher<>(dispatcher.with(key, resultProducer));
   }

   /**
    * Dispatches the call to the registered producer and returns an optional of the result.
    *
    * @param key The key to dispatch for
    *
    * @return An optional over the result
    *
    * @throws NoSuchElementException if no producer has been registered for this key
    */
   public Optional<TargetT> dispatch(KeyT key) throws NoSuchElementException {
      return Optional.ofNullable(dispatcher
                                     .dispatch(key)
                                     .orElseThrow(NoSuchElementException::new)
                                     .get()
      );
   }
}
