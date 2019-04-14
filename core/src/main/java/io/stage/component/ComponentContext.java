package io.aboutcode.stage.component;

import io.aboutcode.stage.lifecycle.LifeCycleStatus;

/**
 * A context available in the initialization of a {@link Component} that provides common
 * functionality and services provided by the {@link ComponentContainer}.
 */
public interface ComponentContext {
   /**
    * @return the current status of this component in the {@link ComponentContainer}'s lifecycle
    */
   LifeCycleStatus getStatus();

   /**
    * @return the identifier assigned to this component
    */
   Object getIdentifier();

   /**
    * Request the termination of the corresponding component container. When and whether the
    * termination takes place is at the discretion of the container.
    */
   void requestTermination(String reason, Exception cause);
}
