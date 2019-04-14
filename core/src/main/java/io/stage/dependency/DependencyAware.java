package io.aboutcode.stage.dependency;

/**
 * A module of an application that is aware of and manages its dependencies to other modules.
 */
public interface DependencyAware {
   /**
    * <p>This method will be called by the infrastructure to identify dependencies of this module
    * to other modules.</p>
    */
   default void resolve(DependencyContext context) throws DependencyException {

   }
}
