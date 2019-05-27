package io.aboutcode.stage.component;

import io.aboutcode.stage.dependency.DependencyAware;
import io.aboutcode.stage.dependency.DependencyContext;
import io.aboutcode.stage.dependency.DependencyException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This allows creating a tree of dependencies, with dependencies having dependencies themselves.
 */
class DependencyTreeBuilder {
    /**
     * Creates the tree of dependencies using the elements specified at construction.
     *
     * @param elements The elements to compile a dependencie tree from
     * @return A list of identifiers in the order in which they should be initialized
     * @throws DependencyException Thrown if a circular dependency is detected or if a required dependency could not be
     *                             found
     */
    static List<Object> buildTree(Map<Object, DependencyAware> elements) throws DependencyException {
        final LinkedHashSet<Object> processedElements = new LinkedHashSet<>();
        for (Object identifier : elements.keySet()) {
            process(identifier, elements, new ArrayDeque<>(), processedElements);
        }
        return new ArrayList<>(processedElements);
    }

    private static void process(Object identifier,
                                Map<Object, DependencyAware> allElements,
                                Deque<Object> currentElementStack,
                                LinkedHashSet<Object> processedElements) throws DependencyException {
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
            element.resolve(new DependencyContext() {
                @Override
                public <DependencyT> DependencyT retrieveDependency(Class<DependencyT> type)
                        throws DependencyException {
                    return retrieveDependency(type, true);
                }

                @Override
                public <DependencyT> DependencyT retrieveDependency(Class<DependencyT> type,
                                                                    boolean required)
                        throws DependencyException {
                    Collection<DependencyT> allDependencies = findDependencies(type);
                    if (allDependencies.isEmpty()) {
                        if (required) {
                            throw new DependencyException(
                                    String.format("Missing dependency of type '%s' for component '%s'", type
                                            .getName(), identifier));
                        }
                        // we don't have the dependency, but we don't care
                        return null;
                    } else if (allDependencies.size() > 1) {
                        throw new DependencyException(String.format(
                                "Multiple matching dependencies of type '%s' found for component '%s'", type
                                        .getName(), identifier));
                    }

                    DependencyT dependency = allDependencies.iterator().next();
                    Object componentIdentifier = null;
                    synchronized (allElements) {
                        for (Map.Entry<Object, DependencyAware> entry : allElements.entrySet()) {
                            if (entry.getValue().equals(dependency)) {
                                componentIdentifier = entry.getKey();
                                break;
                            }
                        }
                    }
                    assert componentIdentifier != null;
                    process(componentIdentifier, allElements, currentElementStack, processedElements);
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
                                    processedElements);
                            returner = (DependencyT) element;
                        }
                    }
                    return returner;
                }

                @Override
                public <DependencyT> Set<DependencyT> retrieveDependencies(Class<DependencyT> type)
                        throws DependencyException {
                    Set<DependencyT> allDependencies = new HashSet<>(findDependencies(type));
                    for (DependencyT dependency : allDependencies) {
                        Object componentIdentifier = null;
                        synchronized (allElements) {
                            for (Map.Entry<Object, DependencyAware> entry : allElements.entrySet()) {
                                if (entry.getValue().equals(dependency)) {
                                    componentIdentifier = entry.getKey();
                                }
                            }
                        }
                        assert componentIdentifier != null;
                        process(componentIdentifier, allElements, currentElementStack, processedElements);
                    }
                    return allDependencies;
                }

                @SuppressWarnings("unchecked")
                private <DependencyT> Collection<DependencyT> findDependencies(
                        Class<DependencyT> clazz) {
                    ArrayList<DependencyAware> elements;
                    synchronized (allElements) {
                        elements = new ArrayList<>(allElements.values());
                    }
                    return elements.stream()
                                   .filter(dependency -> clazz.isAssignableFrom(dependency.getClass()))
                                   .map(dependency -> (DependencyT) dependency)
                                   .collect(Collectors.toList());
                }
            });

            processedElements.add(currentElementStack.pop());
        }
    }
}
