package io.aboutcode.stage.dependency;

import java.util.Set;

/**
 * This provides functionality to allow a unit of an application to resolve dependencies to other
 * units.
 */
public interface DependencyContext {
   /**
    * Retrieve a module dependency based on the specified type and require it to be started before
    * this module.
    *
    * @param type The type of dependency to retrieve
    *
    * @throws DependencyException Thrown if more than one module implementing the specified type is
    *                             found or if any other exception occurs while retrieving the
    *                             dependency
    */
   <DependencyT> DependencyT retrieveDependency(Class<DependencyT> type)
       throws DependencyException;

   /**
    * Retrieve a module dependency based on the specified type and require it to be started before
    * this module. If required is false, this will return null if the requested dependency is not
    * found
    *
    * @param type     The type of dependency to retrieve
    * @param required If true, the component is required to be present and an exception will be
    *                 thrown if it cannot be found at runtime
    *
    * @throws DependencyException Thrown if more than one module implementing the specified type is
    *                             found or if any other exception occurs while retrieving the
    *                             dependency
    */
   <DependencyT> DependencyT retrieveDependency(Class<DependencyT> type, boolean required)
       throws DependencyException;

   /**
    * Retrieve a module dependency based on the specified type and identifier and require it to be
    * started before this module. If optional is true, this will return null if the requested
    * dependency is not found
    *
    * @param identifier An additional identifier for the dependency. This will be used to identify
    *                   the correct dependency if multiple dependencies of the same type are found
    * @param type       The type of dependency to retrieve
    * @param required   If true, the component is required to be present and an exception will be
    *                   thrown if it cannot be found at runtime
    *
    * @throws DependencyException Thrown if any exception occurs while retrieving the dependency
    */
   <DependencyT> DependencyT retrieveDependency(String identifier,
                                                Class<DependencyT> type,
                                                boolean required) throws DependencyException;

   /**
    * Retrieve all module dependencies of the specified type and require them to be started before
    * this module.
    *
    * @throws DependencyException Thrown if any exception occurs while retrieving the dependency
    */
   <DependencyT> Set<DependencyT> retrieveDependencies(Class<DependencyT> type)
       throws DependencyException;
}
