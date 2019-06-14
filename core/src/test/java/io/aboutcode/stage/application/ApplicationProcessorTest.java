package io.aboutcode.stage.application;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.aboutcode.stage.component.BaseComponent;
import io.aboutcode.stage.component.ComponentBundle;
import io.aboutcode.stage.configuration.ApplicationConfigurationContext;
import io.aboutcode.stage.configuration.ConfigurationParameter;
import java.util.List;
import java.util.function.Consumer;
import org.junit.Before;
import org.junit.Test;

public class ApplicationProcessorTest {
    private ApplicationProcessor processor;
    private Application application;
    private ApplicationAssemblyContext applicationAssemblyContext;

    @Before
    public void setUp() throws Exception {
        application = mock(Application.class);
        applicationAssemblyContext = mock(ApplicationAssemblyContext.class);
        processor = ApplicationProcessor.from(application);
    }

    @Test
    public void testEmptyAll() {
        List<ConfigurationParameter> configurationParameters = processor.configure();
        assertTrue(configurationParameters.isEmpty());
        processor.assemble(applicationAssemblyContext);

        verify(applicationAssemblyContext, times(0)).addComponent(any());
        verify(applicationAssemblyContext, times(0)).addComponent(any(), any());
    }

    @Test
    public void testEmptyBundle() {
        ComponentBundle testBundle = new ComponentBundle() {};
        configure(applicationAssemblyContext -> applicationAssemblyContext
                .addComponentBundle(testBundle));

        List<ConfigurationParameter> configurationParameters = processor.configure();
        assertTrue(configurationParameters.isEmpty());
        processor.assemble(applicationAssemblyContext);

        verify(applicationAssemblyContext, times(0)).addComponent(any());
        verify(applicationAssemblyContext, times(0)).addComponent(any(), any());
    }

    @Test
    public void testSimpleBundle() {
        ComponentBundle testBundle = new ComponentBundle() {
            @Override
            public void configure(ApplicationConfigurationContext context) {
                context.addConfigurationParameter(
                        ConfigurationParameter.Option("test", "test", aBoolean -> {}));
            }

            @Override
            public void assemble(ApplicationAssemblyContext context) {
                context.addComponent(new BaseComponent() {});
            }
        };
        configure(applicationAssemblyContext -> applicationAssemblyContext
                .addComponentBundle(testBundle));

        List<ConfigurationParameter> configurationParameters = processor.configure();
        assertFalse(configurationParameters.isEmpty());
        assertEquals(1, configurationParameters.size());

        processor.assemble(applicationAssemblyContext);
        verify(applicationAssemblyContext, times(1)).addComponent(any());
        verify(applicationAssemblyContext, times(0)).addComponent(any(), any());
    }

    @Test
    public void testDeepBundle() {
        ComponentBundle testBundle = new ComponentBundle() {
            @Override
            public void configure(ApplicationConfigurationContext context) {
                context.addConfigurationParameter(
                        ConfigurationParameter.Option("test2", "test2", aBoolean -> {}));
                context.addComponentBundle(new ComponentBundle() {
                    @Override
                    public void configure(ApplicationConfigurationContext context) {
                        context.addConfigurationParameter(
                                ConfigurationParameter.Option("test", "test", aBoolean -> {}));
                    }

                    @Override
                    public void assemble(ApplicationAssemblyContext context) {
                        context.addComponent(new BaseComponent() {});
                    }
                });
            }

            @Override
            public void assemble(ApplicationAssemblyContext context) {
                context.addComponent(new BaseComponent() {});
                context.addComponent(new BaseComponent() {});
            }
        };
        configure(applicationAssemblyContext -> applicationAssemblyContext
                .addComponentBundle(testBundle));

        List<ConfigurationParameter> configurationParameters = processor.configure();
        assertFalse(configurationParameters.isEmpty());
        assertEquals(2, configurationParameters.size());

        processor.assemble(applicationAssemblyContext);
        verify(applicationAssemblyContext, times(3)).addComponent(any());
        verify(applicationAssemblyContext, times(0)).addComponent(any(), any());
    }

    private void configure(Consumer<ApplicationConfigurationContext> configuration) {
        doAnswer(invocation -> {
            configuration.accept((ApplicationConfigurationContext) invocation.getArguments()[0]);
            return null;
        }).when(application).configure(any());
    }
}