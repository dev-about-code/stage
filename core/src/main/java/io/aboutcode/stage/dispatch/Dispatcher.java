package io.aboutcode.stage.dispatch;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This provides facilities to dispatch method invocations based on an identifying property. This
 * allows for indirections without the need of visitors or switch-case statements.
 */
public final class Dispatcher<KeyT, TargetT> {
   private final Map<KeyT, TargetT> registeredDispatches;

   private Dispatcher(Map<KeyT, TargetT> registeredDispatches) {
      this.registeredDispatches = registeredDispatches;
   }

   /**
    * Creates a new empty dipatcher.
    *
    * @param <KeyT>    The type of the key
    * @param <TargetT> The type of the result
    *
    * @return A new dispatcher without any registered results
    */
   public static <KeyT, TargetT> Dispatcher<KeyT, TargetT> empty() {
      return new Dispatcher<>(new HashMap<>());
   }

   /**
    * Creates a new dipatcher with the specified key to result mapping.
    *
    * @param key       The key to add
    * @param result    The result to add
    * @param <KeyT>    The type of the key
    * @param <TargetT> The type of the result
    *
    * @return A new dispatcher with only the specified key to result mapping.
    */
   public static <KeyT, TargetT> Dispatcher<KeyT, TargetT> of(KeyT key, TargetT result) {
      HashMap<KeyT, TargetT> map = new HashMap<>();
      map.put(key, result);
      return new Dispatcher<>(map);
   }

   /**
    * Creates a new Dispatcher from the current dispatcher and adds the specified key to result
    * mapping to the existing mappings of this dispatcher.
    *
    * @param key    The key to add
    * @param result The result to add
    *
    * @return A new dispatcher based on this dispatcher with the specified key to result mapping
    * added.
    */
   public Dispatcher<KeyT, TargetT> with(KeyT key, TargetT result) {
      HashMap<KeyT, TargetT> map = new HashMap<>(registeredDispatches);
      map.put(key, result);
      return new Dispatcher<>(map);
   }

   /**
    * Returns an optional of the result.
    *
    * @param key The key to return the result for
    *
    * @return An optional over the result
    */
   public Optional<TargetT> dispatch(KeyT key) {
      return Optional.ofNullable(registeredDispatches.get(key));
   }
}
