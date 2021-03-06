package io.aboutcode.stage.component;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import io.aboutcode.stage.dependency.DependencyAware;
import io.aboutcode.stage.dependency.DependencyContext;
import io.aboutcode.stage.dependency.DependencyException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.InOrder;
import org.mockito.Mockito;


public class ComponentContainerTest {
    private ComponentContainer container;

    @Before
    public void setUp() {
        container = new ComponentContainer("TEST", () -> {
            throw new IllegalStateException("Cannot shutdown");
        });
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testStartOrder() {
        List<String> order = new ArrayList<>();
        Consumer<String> startFunction = order::add;
        container.addComponent("A", new ResolvingComponent("A", startFunction, "B"));
        container.addComponent("B", new ResolvingComponent("B", startFunction, "C"));
        container.addComponent("C", new ResolvingComponent("C", startFunction, "D"));
        container.addComponent("D", new ResolvingComponent("D", startFunction));
        container.addComponent("E", new ResolvingComponent("E", startFunction, "D"));
        container.addComponent("F", new ResolvingComponent("F", startFunction, "A", "E"));
        container.start();
        container.stop();

        assertOrder(order, "D", "C", "B", "A", "E", "F");
    }

    @Test
    public void testDefaultResolve() {
        AtomicReference<EmptyComponent> resolvedComponent = new AtomicReference<>();
        EmptyComponent defaultComponent = new EmptyComponent();
        container.addComponent(null, defaultComponent);
        container.addComponent("A", new EmptyComponent());
        container.addComponent("B", new BaseComponent() {
            @Override
            public void resolve(DependencyContext context) throws DependencyException {
                resolvedComponent.set(context.retrieveDependency(EmptyComponent.class));
            }
        });
        container.start();
        assertTrue(container.isRunning());
        container.stop();

        assertNotNull(resolvedComponent.get());
        assertEquals(defaultComponent, resolvedComponent.get());
    }

    @Test
    public void testDefaultResolveInvalid() {
        AtomicReference<EmptyComponent> resolvedComponent = new AtomicReference<>();
        EmptyComponent defaultComponent = new EmptyComponent();
        container.addComponent("X", defaultComponent);
        container.addComponent("A", new EmptyComponent());
        container.addComponent("B", new BaseComponent() {
            @Override
            public void resolve(DependencyContext context) throws DependencyException {
                resolvedComponent.set(context.retrieveDependency(EmptyComponent.class));
            }
        });
        container.start();
        assertFalse(container.isRunning());

        assertNull(resolvedComponent.get());
    }

    private void assertOrder(List<String> result, String... expected) {
        Assert.assertArrayEquals(expected, result.toArray(new String[0]));
    }

    @Test
    public void testAllOK() throws Exception {
        Component testComponent = Mockito.mock(Component.class);

        container.addComponent("TestComponent", testComponent);
        container.start();
        container.stop();

        InOrder inOrder = Mockito.inOrder(testComponent);
        inOrder.verify(testComponent, Mockito.times(1)).init(Mockito.any(ComponentContext.class));
        inOrder.verify(testComponent, Mockito.times(1)).start();
        inOrder.verify(testComponent, Mockito.times(1)).stop();
        inOrder.verify(testComponent, Mockito.times(1)).destroy();
    }

    @Test
    public void testFailingInit() throws Exception {
        Component testComponent = Mockito.mock(Component.class);
        Mockito.doThrow(NullPointerException.class).when(testComponent)
               .init(Mockito.any(ComponentContext.class));
        container.addComponent("TestComponent", testComponent);
        try {
            container.start();
        } catch (IllegalStateException e) {
            // ignore
        } finally {
            container.stop();
        }

        InOrder inOrder = Mockito.inOrder(testComponent);
        inOrder.verify(testComponent, Mockito.times(1)).init(Mockito.any(ComponentContext.class));
        inOrder.verify(testComponent, Mockito.times(0)).start();
        inOrder.verify(testComponent, Mockito.times(0)).stop();
        inOrder.verify(testComponent, Mockito.times(1)).destroy();

    }

    @Test
    public void testFailingStart() throws Exception {
        Component testComponent = Mockito.mock(Component.class);
        Mockito.doThrow(NullPointerException.class).when(testComponent).start();
        container.addComponent("TestComponent", testComponent);
        try {
            container.start();
        } catch (IllegalStateException e) {
            // ignore
            fail(e.getMessage());
        } finally {
            container.stop();
        }

        InOrder inOrder = Mockito.inOrder(testComponent);
        inOrder.verify(testComponent, Mockito.times(1)).init(Mockito.any(ComponentContext.class));
        inOrder.verify(testComponent, Mockito.times(1)).start();
        inOrder.verify(testComponent, Mockito.times(1)).stop();
        inOrder.verify(testComponent, Mockito.times(1)).destroy();

    }

    @Test
    public void testContextAvailable() throws Exception {
        Component testComponent = Mockito.mock(Component.class);
        container.addComponent("TestComponent", testComponent);
        try {
            container.start();
        } catch (IllegalStateException e) {
            // ignore
        } finally {
            container.stop();
        }
        Mockito.verify(testComponent).init(Mockito.argThat(new ArgumentMatcher<ComponentContext>() {
            @Override
            public boolean matches(Object o) {
                ComponentContext context = (ComponentContext) o;
                return context != null;
            }
        }));
    }

    private static class ResolvingComponent extends BaseComponent {
        private String name;
        private Consumer<String> startFunction;
        private List<String> componentsToResolve;

        ResolvingComponent(String name, Consumer<String> startFunction,
                           String... componentsToResolve) {
            this.name = name;
            this.startFunction = startFunction;
            this.componentsToResolve = Arrays.asList(componentsToResolve);
        }

        @Override
        public void start() {
            startFunction.accept(name);
        }

        @Override
        public void resolve(DependencyContext context) throws DependencyException {
            for (String component : componentsToResolve) {
                context.retrieveDependency(component, DependencyAware.class, true);
            }
        }
    }

    private static class EmptyComponent extends BaseComponent {

    }
}