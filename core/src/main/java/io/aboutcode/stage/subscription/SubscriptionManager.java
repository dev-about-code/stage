package io.aboutcode.stage.subscription;

import io.aboutcode.stage.concurrent.PooledTopicExecutor;
import io.aboutcode.stage.concurrent.TopicExecutor;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * <p>Standard implementation of a subscriber/consumer pattern that allows for synchronous or
 * asynchronous execution of tasks. Synchronous execution forces the publisher of an event to wait
 * for all listener notifications to be finished while asynchronous execution returns to the
 * publisher immediately. Subscriptions can be generic (for all listeners) or topic bound:
 * notifications published to a certain topic will only be escalated to subscribers of that
 * topic.</p>
 *
 * <p>Generation of subscription handles can be customized by specifying a factory for handle
 * generation. A default handle factory is available and generates incrementing long numbers.
 * However, no assumption should be made on the generated default handles when it comes to sequence,
 * order or size.</p>
 *
 * @param <HandleT>   The type of handle this subscription manager uses for identification of
 *                    subscribers (standard: Long)
 * @param <ListenerT> The listener type that this manager uses for consuming published events
 * @param <HandbackT> The type of handback that this manager return to events
 */
public final class SubscriptionManager<HandleT, ListenerT, HandbackT> {
   private static final TopicExecutor DEFAULT_EXECUTOR = new TopicExecutor() {
      @Override
      public void execute(Object topic, Runnable task) {
         execute(task);
      }

      @Override
      public void execute(Runnable task) {
         task.run();
      }
   };
   private final Map<Object, List<ListenerWrapper>> topicSubscriptions = new HashMap<>();
   private final Map<HandleT, ListenerWrapper> listeners = new HashMap<>();
   private final HandleFactory<HandleT> handleFactory;
   private final Object monitor = new Object();

   private final TopicExecutor executor;

   private SubscriptionManager(HandleFactory<HandleT> handleFactory, TopicExecutor executor) {
      this.executor = executor;
      this.handleFactory = handleFactory;
   }

   /**
    * A standard handle factory that generates incrementing long values.
    */
   public static HandleFactory<Long> IncrementingLongHandleFactory() {
      AtomicLong handleGenerator = new AtomicLong();
      return handleGenerator::incrementAndGet;
   }

   /**
    * Creates a subscription manager that executes tasks synchronously, using the default long
    * handle factory.
    *
    * @param <ListenerT> The listener type this subscription manager uses to consume published
    *                    events.
    */
   public static <ListenerT, HandbackT> SubscriptionManager<Long, ListenerT, HandbackT> synchronous() {
      return synchronous(IncrementingLongHandleFactory());
   }

   /**
    * Creates a subscription manager that executes tasks asynchronously (without the publisher
    * waiting for execution), using the default long handle factory.
    *
    * @param <ListenerT> The listener type this subscription manager uses to consume published
    *                    events.
    */
   public static <ListenerT, HandbackT> SubscriptionManager<Long, ListenerT, HandbackT> asynchronous() {
      return asynchronous(IncrementingLongHandleFactory());
   }

   /**
    * Creates a subscription manager that executes tasks synchronously, using the specified handle
    * factory.
    *
    * @param <HandleT>   The type of handle this subscription manager uses for identification of
    *                    subscribers
    * @param <ListenerT> The listener type this subscription manager uses to consume published
    *                    events.
    */
   public static <HandleT, ListenerT, HandbackT> SubscriptionManager<HandleT, ListenerT, HandbackT> synchronous(
       HandleFactory<HandleT> handleFactory) {
      return new SubscriptionManager<>(handleFactory, DEFAULT_EXECUTOR);
   }

   /**
    * Creates a subscription manager that executes tasks asynchronously (without the publisher
    * waiting for execution), using the specified handle factory.
    *
    * @param <HandleT>   The type of handle this subscription manager uses for identification of
    *                    subscribers
    * @param <ListenerT> The listener type this subscription manager uses to consume published
    *                    events.
    */
   public static <HandleT, ListenerT, HandbackT> SubscriptionManager<HandleT, ListenerT, HandbackT> asynchronous(
       HandleFactory<HandleT> handleFactory) {
      return new SubscriptionManager<>(handleFactory, new PooledTopicExecutor());
   }

   /**
    * Returns all topics that currently have at least one active subscription.
    *
    * @return All topics that currently have at lest one active subscription
    */
   public Set<Object> getTopics() {
      synchronized (monitor) {
         return topicSubscriptions
             .entrySet()
             .stream()
             .filter(entry -> !entry.getValue().isEmpty())
             .collect(Collectors.toSet());
      }
   }

   /**
    * Subscribes the specified listener to the specified topic. The specified handback will be
    * available to the listener for each event notification.
    *
    * @param topic    The topic to subscribe to
    * @param handback The handback
    * @param listener The listener to notify for an event
    *
    * @return The handle that can be used to unsubscribe the listener
    */
   public HandleT subscribe(Object topic, HandbackT handback, ListenerT listener) {
      HandleT handle = handleFactory.create();
      ListenerWrapper listenerWrapper = new ListenerWrapper(handle, listener, handback, topic);
      synchronized (monitor) {
         List<ListenerWrapper> topicListeners;
         if (listeners.containsKey(handle)) {
            throw new IllegalArgumentException(String.format(
                "Listener with handle '%s' already is subscribed - handle factory might not produce unique results",
                handle));
         }
         listeners.put(handle, listenerWrapper);
         if (!topicSubscriptions.containsKey(topic)) {
            topicListeners = new LinkedList<>();
            topicSubscriptions.put(topic, topicListeners);
         } else {
            topicListeners = topicSubscriptions.get(topic);
         }
         topicListeners.add(listenerWrapper);
      }
      return handle;
   }

   /**
    * Removes all subscriptions for all topics from this subscription manager.
    */
   public void clear() {
      synchronized (monitor) {
         topicSubscriptions.clear();
         listeners.clear();
      }
   }

   /**
    * Unsubscribes the listener identified by the specified handle from this subscription manager.
    *
    * @param handle The handle to unsubscribe the listener for
    */
   public void unsubscribe(HandleT handle) {
      synchronized (monitor) {
         if (listeners.containsKey(handle)) {
            ListenerWrapper listenerWrapper = listeners.remove(handle);
            List<ListenerWrapper> topicSubscriptions = this.topicSubscriptions
                .get(listenerWrapper.topic);
            topicSubscriptions.remove(listenerWrapper);
         }
      }
   }

   /**
    * Notifies all subscribers to the specified topic of the specified event.
    *
    * @param topic  The topic that subscribers should be notified for.
    * @param action The action to execute for each listener
    */
   public void forTopic(Object topic, SubscriptionAction<ListenerT, HandbackT> action) {
      List<ListenerWrapper> subscribers = new LinkedList<>();
      synchronized (monitor) {
         if (topicSubscriptions.containsKey(topic)) {
            subscribers.addAll(topicSubscriptions.get(topic));
         }
      }

      subscribers
          .forEach(wrapper ->
                       executor
                           .execute(topic,
                                    () -> action.apply(wrapper.listener,
                                                       wrapper.handback,
                                                       () -> unsubscribe(wrapper.handle))
                           )
          );
   }

   /**
    * Notifies <em>all</em> subscribers for <em>all</em> topics of the specified event.
    *
    * @param action The action to execute for each listener
    */
   public void forAll(SubscriptionAction<ListenerT, HandbackT> action) {
      final List<ListenerWrapper> subscribers;
      synchronized (monitor) {
         subscribers = topicSubscriptions
             .values()
             .stream()
             .flatMap(Collection::stream)
             .collect(Collectors.toList());
      }
      subscribers
          .forEach(wrapper ->
                       executor
                           .execute(() -> action.apply(wrapper.listener,
                                                       wrapper.handback,
                                                       () -> unsubscribe(wrapper.handle))
                           )
          );
   }

   /**
    * The factory used to generate a handle for each subscriber
    *
    * @param <HandleT> The type of handle to generate
    */
   public interface HandleFactory<HandleT> {
      HandleT create();
   }

   /**
    * An event that is published to listeners.
    *
    * @param <ListenerT> The type of listener to promote the event to
    */
   public interface SubscriptionAction<ListenerT, HandbackT> {
      /**
       * An action to be applied for each subscriber.
       *
       * @param subscriber The subscriber to apply the action on
       * @param handback   The handback initially passed to the subscription manager when the
       *                   subscriber was registered
       * @param context    The context for this subscriber
       */
      void apply(ListenerT subscriber, HandbackT handback, SubscriberContext context);
   }

   /**
    * A context that allows cancellation of a subscription. This is passed to the forXXX methods to
    * allow interaction with the subscription manager from within those methods.
    */
   public interface SubscriberContext {
      /**
       * Cancels the subscription of the current subscriber.
       */
      void cancel();
   }

   private class ListenerWrapper {
      private ListenerT listener;
      private HandbackT handback;
      private Object topic;
      private HandleT handle;

      private ListenerWrapper(HandleT handle,
                              ListenerT listener,
                              HandbackT handback,
                              Object topic) {
         this.handle = handle;
         this.listener = listener;
         this.handback = handback;
         this.topic = topic;
      }
   }
}
