package io.aboutcode.stage.lifecycle;

/**
 * <p>A component that follows a lifecycle of initialization, startup, decomposition and
 * destruction.</p>
 *
 * <p><em>Note: The implementing unit must take care that all lifecycle methods terminate in a
 * timely manner!</em></p>
 *
 * @param <ContextT> the type of context passed to the code at initialization time
 */
public interface LifeCycleAware<ContextT> {
   /**
    * The implementing code should use this method to allocate resources and generally prepare the
    * component for startup. This includes - for example - connecting to external dependencies,
    * creating files, etc.
    */
   default void init(ContextT context) throws LifeCycleException {

   }

   /**
    * Any actual work, including starting threads and processing data, should be performed in this
    * method.
    */
   default void start() throws LifeCycleException {

   }

   /**
    * Any decomposition of the component - like persisting a final state in a database, for example
    * - should be performed in this method.
    */
   default void stop() {

   }

   /**
    * Any cleanup and releasing of resources must be performed in this method. After this method
    * returns, the unit will be discarded.
    */
   default void destroy() {

   }
}
