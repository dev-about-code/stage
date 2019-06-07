package io.aboutcode.stage.dependency;

/**
 * A module of an application that is aware of and manages its dependencies to other modules.
 */
public interface DependencyAware {
    /**
     * <p>This method will be called by the infrastructure to identify dependencies of this module
     * to other modules.</p>
     *
     * @param context The context to retrieve dependencies through
     *
     * @throws DependencyException If not all dependencies could be satisfied
     */
    default void resolve(DependencyContext context) throws DependencyException {

    }
}
