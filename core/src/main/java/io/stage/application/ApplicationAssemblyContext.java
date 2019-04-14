package io.aboutcode.stage.application;

import io.aboutcode.stage.component.Component;

/**
 * Context for assembling an application before starting and executing it
 */
public interface ApplicationAssemblyContext {

   /**
    * Adds a component to the application that will be managed by the application and is available
    * in the global context of the application. Only one component with the specified identifier may
    * be present in any {@link ApplicationContainer}
    *
    * @param identifier The unique identifier of the component
    * @param component  The component to add
    */
   void addComponent(Object identifier, Component component);

   /**
    * Adds a component to the application that will be managed by the application and is available
    * in the global context of the application. It will not have an identifier assigned, hence there
    * may be only one instance of this class in the context.
    *
    * @param component The component to add
    */
   void addComponent(Component component);
}
