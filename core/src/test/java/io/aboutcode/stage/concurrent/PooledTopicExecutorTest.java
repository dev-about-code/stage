package io.aboutcode.stage.concurrent;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PooledTopicExecutorTest {
    @Test
    public void testSequentialProcessing() throws Exception {
        StringBuilder topicOneResult = new StringBuilder();
        StringBuilder topicTwoResult = new StringBuilder();
        SignalCondition conditionOne = new SignalCondition();
        SignalCondition conditionTwo = new SignalCondition();

        PooledTopicExecutor threadPool = new PooledTopicExecutor();
        threadPool.execute(topicOneResult, () -> incrementAndSleep(topicOneResult, "1"));
        threadPool.execute(topicOneResult, () -> incrementAndSleep(topicOneResult, "2"));
        threadPool.execute(topicOneResult, () -> incrementAndSleep(topicOneResult, "3"));
        threadPool.execute(topicOneResult, () -> incrementAndSleep(topicOneResult, "4"));
        threadPool.execute(topicOneResult, () -> {
            incrementAndSleep(topicOneResult, "5");
            conditionOne.signalAll();
        });

        threadPool.execute(topicTwoResult, () -> incrementAndSleep(topicTwoResult, "5"));
        threadPool.execute(topicTwoResult, () -> incrementAndSleep(topicTwoResult, "4"));
        threadPool.execute(topicTwoResult, () -> incrementAndSleep(topicTwoResult, "3"));
        threadPool.execute(topicTwoResult, () -> incrementAndSleep(topicTwoResult, "2"));
        threadPool.execute(topicTwoResult, () -> {
            incrementAndSleep(topicTwoResult, "1");
            conditionTwo.signalAll();
        });

        conditionOne.await();
        conditionTwo.await();

        assertEquals("12345", topicOneResult.toString());
        assertEquals("54321", topicTwoResult.toString());
    }

    private void incrementAndSleep(StringBuilder target, String value) {
        target.append(value);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}