package io.aboutcode.stage.dependency;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Collection;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class AnnotatedDependencyParserTest {
    private DependencyContext context;

    @Before
    public void setUp() {
        context = mock(DependencyContext.class);
    }

    @Test
    public void testNoAnnotation() {
        Collection<DependencyAware> dependencyAwares = AnnotatedDependencyParser
                .parseAnnotations(new Test1());

        assertNotNull(dependencyAwares);
        assertTrue(dependencyAwares.isEmpty());
        verifyNoMoreInteractions(context);
    }

    @Test
    public void testSingleValue() throws Exception {
        Collection<DependencyAware> dependencyAwares = AnnotatedDependencyParser
                .parseAnnotations(new Test2());

        assertNotNull(dependencyAwares);
        assertEquals(1, dependencyAwares.size());
        for (DependencyAware aware : dependencyAwares) {
            aware.resolve(context);
        }
        verify(context, times(1)).retrieveDependency(eq(String.class), eq(true));
        verifyNoMoreInteractions(context);
    }

    @Test
    public void testCollection() throws Exception {
        Collection<DependencyAware> dependencyAwares = AnnotatedDependencyParser
                .parseAnnotations(new Test3());

        assertNotNull(dependencyAwares);
        assertEquals(1, dependencyAwares.size());
        for (DependencyAware aware : dependencyAwares) {
            aware.resolve(context);
        }
        verify(context, times(1)).retrieveDependencies(eq(String.class));
        verifyNoMoreInteractions(context);
    }

    @Test
    public void testArray() throws Exception {
        Collection<DependencyAware> dependencyAwares = AnnotatedDependencyParser
                .parseAnnotations(new Test4());

        assertNotNull(dependencyAwares);
        assertEquals(1, dependencyAwares.size());
        for (DependencyAware aware : dependencyAwares) {
            aware.resolve(context);
        }
        verify(context, times(1)).retrieveDependencies(eq(String.class));
        verifyNoMoreInteractions(context);
    }

    @Test
    public void testPrimitiveArray() throws Exception {
        Collection<DependencyAware> dependencyAwares = AnnotatedDependencyParser
                .parseAnnotations(new Test5());

        assertNotNull(dependencyAwares);
        assertEquals(1, dependencyAwares.size());
        for (DependencyAware aware : dependencyAwares) {
            aware.resolve(context);
        }
        verify(context, times(1)).retrieveDependencies(eq(long.class));
        verifyNoMoreInteractions(context);
    }

    @Test
    public void testExtends() throws Exception {
        Collection<DependencyAware> dependencyAwares = AnnotatedDependencyParser
                .parseAnnotations(new Test6());

        assertNotNull(dependencyAwares);
        assertEquals(4, dependencyAwares.size());
        for (DependencyAware aware : dependencyAwares) {
            aware.resolve(context);
        }
        verify(context, times(2)).retrieveDependency(eq(String.class), eq(true));
        verify(context, times(1)).retrieveDependency(eq("test"), eq(long.class), eq(true));
        verify(context, times(1)).retrieveDependency(eq(Test5.class), eq(false));
        verifyNoMoreInteractions(context);
    }

    private interface TestInterface {

    }

    private static class Test1 implements DependencyAware {

    }

    private static class Test2 implements DependencyAware {
        @Resolved
        private String singleValue;
    }

    private static class Test3 implements DependencyAware {
        @Resolved
        private List<String> collection;
    }

    private static class Test4 implements DependencyAware {
        @Resolved
        private String[] array;
    }

    private static class Test5 implements DependencyAware {
        @Resolved
        private long[] array;
    }

    private static class Test6 extends Test2 {
        @Resolved
        private String single;
        @Resolved(identifier = "test")
        private long other;
        @Resolved(mandatory = false)
        private Test5 special;
    }
}