package io.aboutcode.stage.concurrent;

import java.util.concurrent.Executor;


/**
 * Implementations of this allow execution of tasks on a per topic level in parallel. The execution
 * is guaranteed to be exactly identical to the order of submission of tasks for that topic.
 */
public interface TopicExecutor extends Executor {
   /**
    * Executes the specified task on the specified topic with guaranteed order of submission.
    */
   void execute(Object topic, Runnable task);
}
