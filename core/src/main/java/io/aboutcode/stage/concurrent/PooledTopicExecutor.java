package io.aboutcode.stage.concurrent;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the default implementation of a {@link TopicExecutor} and internally uses a threadpool
 * for execution. The threadpool is shared among all registered topics.
 */
public final class PooledTopicExecutor implements TopicExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(PooledTopicExecutor.class);
    private static final String DEFAULT_TOPIC = null;
    private final Map<Object, ProcessingQueue> topics = new HashMap<>();
    private final Object monitor = new Object();

    /**
     * Executes the specified task on the default topic.
     */
    @Override
    public void execute(Runnable task) {
        execute(DEFAULT_TOPIC, task);
    }

    /**
     * Executes the specified task on the specified topic with guaranteed order of submission.
     */
    public void execute(Object topic, Runnable task) {
        ProcessingQueue queue;
        synchronized (monitor) {
            if (!topics.containsKey(topic)) {
                queue = new ProcessingQueue(topic);
                topics.put(topic, queue);
            } else {
                queue = topics.get(topic);
            }
        }
        queue.add(task);
        queue.promote(false);
    }

    private class ProcessingQueue {
        private final Queue<Runnable> queue = new LinkedList<>();
        private final Object monitor = new Object();
        private boolean active;
        private Object topic;

        private ProcessingQueue(Object topic) {
            this.topic = topic;
        }

        public void add(Runnable task) {
            synchronized (monitor) {
                queue.add(task);
            }
        }

        private void promote(boolean deactivate) {
            final Runnable nextAction;
            synchronized (monitor) {
                if (deactivate) {
                    active = false;
                }
                if (!active && !queue.isEmpty()) {
                    nextAction = queue.poll();
                    active = true;
                } else {
                    nextAction = null;
                }
            }
            if (nextAction != null) {
                CompletableFuture.runAsync(() -> {
                    try {
                        nextAction.run();
                    } catch (Exception e) {
                        LOGGER.warn(
                                String.format("Executing action in topic '%s' threw exception: %s",
                                              topic, e.getMessage()), e);
                    }
                }).thenAccept(nada -> promote(true));
            }
        }
    }
}