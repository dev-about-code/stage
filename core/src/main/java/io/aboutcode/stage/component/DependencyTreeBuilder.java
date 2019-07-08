package io.aboutcode.stage.component;

import io.aboutcode.stage.dependency.AnnotatedDependencyParser;
import io.aboutcode.stage.dependency.DependencyAware;
import io.aboutcode.stage.dependency.DependencyContext;
import io.aboutcode.stage.dependency.DependencyException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This allows creating a tree of dependencies, with dependencies having dependencies themselves.
 */
class DependencyTreeBuilder {
    /**
     * Creates the tree of dependencies using the elements specified at construction.
     *
     * @param elements                           The elements to compile a dependencie tree from
     * @param defaultDependencyIdentifierCreator Used to generate the default identifier for a type
     *                                           if multiple instances of the type have been added
     *                                           to the container
     *
     * @return A list of identifiers in the order in which they should be initialized
     *
     * @throws DependencyException Thrown if a circular dependency is detected or if a required
     *                             dependency could not be found
     */
    static List<Object> buildTree(Map<Object, DependencyAware> elements,
                                  Function<Object, Object> defaultDependencyIdentifierCreator)
            throws DependencyException {
        final LinkedHashSet<Object> processedElements = new LinkedHashSet<>();
        for (Object identifier : elements.keySet()) {
            process(identifier, elements, new ArrayDeque<>(), processedElements,
                    defaultDependencyIdentifierCreator);
        }
        return new ArrayList<>(processedElements);
    }

    private static void process(Object identifier,
                                Map<Object, DependencyAware> allElements,
                                Deque<Object> currentElementStack,
                                LinkedHashSet<Object> processedElements,
                                Function<Object, Object> defaultDependencyIdentifierCreator)
            throws DependencyException {
        if (!processedElements.contains(identifier)) {
            if (currentElementStack.contains(identifier)) {
                throw new DependencyException(String.format(
                        "Circular dependency detected for elements: %s -> %s",
                        currentElementStack.stream().map(Object::toString)
                                           .collect(Collectors.joining(" -> ")), identifier));
            }

            currentElementStack.push(identifier);
            DependencyAware element;
            synchronized (allElements) {
                element = allElements.get(identifier);
            }

            // create dependency context
            DependencyContext dependencyContext = new DependencyContext() {
                @Override
                public <DependencyT> DependencyT retrieveDependency(Class<DependencyT> type)
                        throws DependencyException {
                    return retrieveDependency(type, true);
                }

                @Override
                public <DependencyT> DependencyT retrieveDependency(Class<DependencyT> type,
                                                                    boolean required)
                        throws DependencyException {
                    Map<Object, DependencyT> allDependencies = findDependencies(type);
                    if (allDependencies.isEmpty()) {
                        if (required) {
                            throw new DependencyException(
                                    String.format(
                                            "Missing dependency of type '%s' for component '%s'",
                                            type
                                                    .getName(), identifier));
                        }
                        // we don't have the dependency, but we don't care
                        return null;
                    }

                    DependencyT dependency;
                    if (allDependencies.size() > 1) {
                        // let's try to retrieve the default dependency for this type
                        dependency = allDependencies
                                .get(defaultDependencyIdentifierCreator.apply(type));

                        // nope, we need to bail out
                        if (dependency == null) {
                            throw new DependencyException(String.format(
                                    "Multiple matching dependencies of type '%s' found for component '%s'",
                                    type.getName(), identifier));
                        }
                    } else {
                        dependency = allDependencies.values().iterator().next();
                    }

                    Object componentIdentifier = null;
                    synchronized (allElements) {
                        for (Entry<Object, DependencyAware> entry : allElements.entrySet()) {
                            if (entry.getValue().equals(dependency)) {
                                componentIdentifier = entry.getKey();
                                break;
                            }
                        }
                    }
                    assert componentIdentifier != null;
                    process(componentIdentifier, allElements, currentElementStack,
                            processedElements, defaultDependencyIdentifierCreator);
                    return dependency;
                }

                @Override
                @SuppressWarnings("unchecked")
                public <DependencyT> DependencyT retrieveDependency(Object componentIdentifier,
                                                                    Class<DependencyT> type,
                                                                    boolean required)
                        throws DependencyException {
                    DependencyT returner = null;
                    DependencyAware element = allElements.get(componentIdentifier);
                    if (element == null && required) {
                        throw new DependencyException(String.format(
                                "Missing dependency of type '%s' with identifier '%s' for component '%s'",
                                type
                                        .getName(), componentIdentifier, identifier));
                    } else if (element != null) {
                        if (!type.isAssignableFrom(element.getClass()) && required) {
                            throw new DependencyException(String.format(
                                    "Dependency of type '%s' with identifier '%s' for component '%s' is not of expected class '%s'",
                                    element.getClass(), componentIdentifier, identifier, type
                                            .getName()));
                        } else if (required) { // means that the element is assignable from clazz
                            // recursively resolve dependencies for this element
                            process(componentIdentifier, allElements, currentElementStack,
                                    processedElements, defaultDependencyIdentifierCreator);
                            returner = (DependencyT) element;
                        }
                    }
                    return returner;
                }

                @Override
                public <DependencyT> Set<DependencyT> retrieveDependencies(Class<DependencyT> type)
                        throws DependencyException {
                    Set<DependencyT> allDependencies = new HashSet<>(
                            findDependencies(type).values());
                    for (DependencyT dependency : allDependencies) {
                        Object componentIdentifier = null;
                        synchronized (allElements) {
                            for (Entry<Object, DependencyAware> entry : allElements
                                    .entrySet()) {
                                if (entry.getValue().equals(dependency)) {
                                    componentIdentifier = entry.getKey();
                                }
                            }
                        }
                        assert componentIdentifier != null;
                        process(componentIdentifier, allElements, currentElementStack,
                                processedElements, defaultDependencyIdentifierCreator);
                    }
                    return allDependencies;
                }

                @SuppressWarnings("unchecked")
                private <DependencyT> Map<Object, DependencyT> findDependencies(
                        Class<DependencyT> clazz) {
                    Map<Object, DependencyAware> elements;
                    synchronized (allElements) {
                        elements = new HashMap<>(allElements);
                    }
                    return elements
                            .entrySet()
                            .stream()
                            .filter(entry -> clazz.isAssignableFrom(entry.getValue().getClass()))
                            .collect(Collectors.toMap(
                                    Entry::getKey,
                                    entry -> (DependencyT) entry.getValue()
                            ));
                }
            };

            // resolve annotated fields
            for (DependencyAware dependencyAware :
                    AnnotatedDependencyParser.parseAnnotations(element)) {
                dependencyAware.resolve(dependencyContext);
            }

            // resolve implemented method
            element.resolve(dependencyContext);

            processedElements.add(currentElementStack.pop());
        }
    }
}
