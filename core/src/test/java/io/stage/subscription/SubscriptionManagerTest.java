package io.aboutcode.stage.subscription;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicLong;
import org.junit.Test;

public class SubscriptionManagerTest {
   private AtomicLong handleGenerator = new AtomicLong();
   private SubscriptionManager<Long, TestListener, Long> subscriptionManager = SubscriptionManager
       .synchronous(handleGenerator::incrementAndGet);

   @Test
   public void testSubscribe() throws Exception {
      String topicOne = "One";
      String topicTwo = "Two";
      TestListener testListenerOne = new TestListener(topicOne);
      TestListener testListenerTwo = new TestListener(topicTwo);
      Long handleOne = subscriptionManager.subscribe(topicOne, 100L, testListenerOne);
      Long handleTwo = subscriptionManager.subscribe(topicTwo, 200L, testListenerTwo);

      subscriptionManager.forAll((listener, handback, context) -> listener.process(handback));
      assertEquals("One=100", testListenerOne.getValue());
      assertEquals("Two=200", testListenerTwo.getValue());

      subscriptionManager
          .forTopic(topicOne, (listener, handback, context) -> listener.process(handback * 2));
      assertEquals("One=200", testListenerOne.getValue());
      assertEquals("Two=200", testListenerTwo.getValue());

      subscriptionManager.unsubscribe(handleTwo);
      subscriptionManager
          .forTopic(topicTwo, (listener, handback, context) -> listener.process(handback * 2));
      assertEquals("One=200", testListenerOne.getValue());
      assertEquals("Two=200", testListenerTwo.getValue());

      subscriptionManager.unsubscribe(handleOne);
      subscriptionManager
          .forTopic(topicOne, (listener, handback, context) -> listener.process(handback * 4));
      assertEquals("One=200", testListenerOne.getValue());
      assertEquals("Two=200", testListenerTwo.getValue());
   }

   @Test
   public void testMultiSubscriber() throws Exception {
      String topic = "Topic";
      TestListener testListenerOne = new TestListener(topic);
      TestListener testListenerTwo = new TestListener(topic);
      Long handleOne = subscriptionManager.subscribe(topic, 100L, testListenerOne);
      Long handleTwo = subscriptionManager.subscribe(topic, 200L, testListenerTwo);

      subscriptionManager.forAll((listener, handback, context) -> listener.process(handback));
      assertEquals("Topic=100", testListenerOne.getValue());
      assertEquals("Topic=200", testListenerTwo.getValue());

      subscriptionManager
          .forTopic(topic, (listener, handback, context) -> listener.process(handback * 2));
      assertEquals("Topic=200", testListenerOne.getValue());
      assertEquals("Topic=400", testListenerTwo.getValue());

      subscriptionManager.unsubscribe(handleOne);
      subscriptionManager
          .forTopic(topic, (listener, handback, context) -> listener.process(handback * 4));
      assertEquals("Topic=200", testListenerOne.getValue());
      assertEquals("Topic=800", testListenerTwo.getValue());
   }

   private class TestListener {
      private String topic;
      private String value;

      public TestListener(String topic) {
         this.topic = topic;
      }

      public void process(long value) {
         this.value = String.format("%s=%d", topic, value);
      }

      public String getValue() {
         return value;
      }
   }
}