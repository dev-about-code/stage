package io.aboutcode.stage.component;

import static org.junit.Assert.assertEquals;

import io.aboutcode.stage.dependency.DependencyAware;
import io.aboutcode.stage.dependency.DependencyContext;
import io.aboutcode.stage.dependency.DependencyException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class DependencyTreeBuilderTest {
    @Test
    public void testSimpleResolving() throws Exception {
        Map<Object, DependencyAware> allComponents = new HashMap<>();
        allComponents.put("A", new ResolvingComponent("B"));
        allComponents.put("B", new DummyComponent());
        testWith(allComponents, "B", "A");
    }

    @Test
    public void testDeepResolving() throws Exception {
        Map<Object, DependencyAware> allComponents = new HashMap<>();
        allComponents.put("A", new ResolvingComponent("B"));
        allComponents.put("B", new ResolvingComponent("C"));
        allComponents.put("C", new ResolvingComponent("D"));
        allComponents.put("D", new DummyComponent());
        testWith(allComponents, "D", "C", "B", "A");
    }

    @Test(expected = DependencyException.class)
    public void testFailedResolving() throws Exception {
        Map<Object, DependencyAware> allComponents = new HashMap<>();
        allComponents.put("A", new ResolvingComponent("B"));
        testWith(allComponents, "B", "A");
    }

    @Test(expected = DependencyException.class)
    public void testFailedDeepResolving() throws Exception {
        Map<Object, DependencyAware> allComponents = new HashMap<>();
        allComponents.put("A", new ResolvingComponent("B"));
        allComponents.put("B", new ResolvingComponent("C"));
        allComponents.put("C", new ResolvingComponent("D"));
        allComponents.put("D", new ResolvingComponent("E"));
        testWith(allComponents, "E", "D", "C", "B", "A");
    }

    @Test(expected = DependencyException.class)
    public void testFailedCircularResolving() throws Exception {
        Map<Object, DependencyAware> allComponents = new HashMap<>();
        allComponents.put("A", new ResolvingComponent("B"));
        allComponents.put("B", new ResolvingComponent("C"));
        allComponents.put("C", new ResolvingComponent("A"));
        testWith(allComponents, "C", "B", "A");
    }

    @Test
    public void testOptionalResolvingWithoutIdentifier() throws Exception {
        Map<Object, DependencyAware> allComponents = new HashMap<>();
        allComponents.put("A", new ResolvingComponent("B"));
        allComponents.put("B", new ResolvingComponent("C"));
        allComponents.put("C", new ResolvingComponent("D"));
        allComponents.put("D", new DummyComponent() {
            @Override
            public void resolve(DependencyContext context) throws DependencyException {
                context.retrieveDependency(Integer.class, false);
            }
        });
        testWith(allComponents, "D", "C", "B", "A");
    }

    @Test
    public void testOptionalResolving() throws Exception {
        Map<Object, DependencyAware> allComponents = new HashMap<>();
        allComponents.put("A", new ResolvingComponent("B"));
        allComponents.put("B", new ResolvingComponent("C"));
        allComponents.put("C", new ResolvingComponent("D"));
        allComponents.put("D", new DummyComponent() {
            @Override
            public void resolve(DependencyContext context) throws DependencyException {
                context.retrieveDependency("E", DependencyAware.class, false);
            }
        });
        testWith(allComponents, "D", "C", "B", "A");
    }

    @Test
    public void testMultiResolving() throws Exception {
        Map<Object, DependencyAware> allComponents = new HashMap<>();
        allComponents.put("A", new ResolvingComponent("B"));
        allComponents.put("B", new ResolvingComponent("C"));
        allComponents.put("C", new ResolvingComponent("D"));
        allComponents.put("E", new DummyComponent2("F"));
        allComponents.put("F", new DummyComponent2());
        allComponents.put("D", new DummyComponent() {
            @Override
            public void resolve(DependencyContext context) throws DependencyException {
                context.retrieveDependencies(DummyComponent2.class);
            }
        });
        testWith(allComponents, "F", "E", "D", "C", "B", "A");
    }

    @Test
    public void testTreeResolving() throws Exception {
        Map<Object, DependencyAware> allComponents = new HashMap<>();
        allComponents.put("A", new ResolvingComponent("B"));
        allComponents.put("B", new ResolvingComponent("C", "D"));
        allComponents.put("C", new ResolvingComponent("E", "F"));
        allComponents.put("D", new ResolvingComponent("E"));
        allComponents.put("E", new ResolvingComponent("F"));
        allComponents.put("F", new ResolvingComponent());
        allComponents.put("G", new ResolvingComponent("E"));
        testWith(allComponents, "F", "E", "C", "D", "B", "A", "G");
    }

    @Test
    public void testForeignClassResolving() throws Exception {
        Map<Object, DependencyAware> allComponents = new HashMap<>();
        allComponents.put("A", new ResolvingComponent("B"));
        allComponents.put("B", new DummyComponent() {
            @Override
            public void resolve(DependencyContext context) throws DependencyException {
                TestMarker tester = context.retrieveDependency("C", TestMarker.class, true);
                tester.doSomething();
            }
        });
        allComponents.put("C", new MarkedComponent());
        testWith(allComponents, "C", "B", "A");
    }

    @Test(expected = DependencyException.class)
    public void testMultipleClassResolving() throws Exception {
        Map<Object, DependencyAware> allComponents = new HashMap<>();
        allComponents.put("A", new DummyComponent());
        allComponents.put("B", new DummyComponent());
        allComponents.put("C", new DummyComponent() {
            @Override
            public void resolve(DependencyContext context) throws DependencyException {
                context.retrieveDependency(DummyComponent.class);
            }
        });
        testWith(allComponents, "C", "B", "A");
    }

    private void testWith(Map<Object, DependencyAware> components, Object... expectedOrder)
            throws DependencyException {
        List<Object> actualOrder = DependencyTreeBuilder.buildTree(components);
        assertEquals(Arrays.asList(expectedOrder), actualOrder);
    }

    private interface TestMarker {
        void doSomething();
    }

    private class MarkedComponent implements DependencyAware, TestMarker {
        @Override
        public void resolve(DependencyContext context) throws DependencyException {
        }

        @Override
        public void doSomething() {

        }
    }

    private class DummyComponent2 extends ResolvingComponent {
        DummyComponent2(String... componentsToResolve) {
            super(componentsToResolve);
        }
    }

    private class DummyComponent implements DependencyAware {
        @Override
        public void resolve(DependencyContext context) throws DependencyException {
        }
    }

    private class ResolvingComponent implements DependencyAware {
        private List<String> componentsToResolve;

        ResolvingComponent(String... componentsToResolve) {
            this.componentsToResolve = Arrays.asList(componentsToResolve);
        }

        @Override
        public void resolve(DependencyContext context) throws DependencyException {
            for (String component : componentsToResolve) {
                context.retrieveDependency(component, DependencyAware.class, true);
            }
        }
    }
}