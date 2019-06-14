package io.aboutcode.stage.application;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import io.aboutcode.stage.configuration.ApplicationConfigurationContext;
import io.aboutcode.stage.configuration.ConfigurationContext;
import io.aboutcode.stage.configuration.ConfigurationParameter;
import io.aboutcode.stage.feature.Feature;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
// TODO: add tests for all code
public class ApplicationContainerTest {
    private Application application;

    @Before
    public void setUp() throws Exception {
        application = mock(Application.class);
    }

    @Test
    public void testFeatures() {
        final AtomicReference<String> result = new AtomicReference<>();
        Feature feature = new Feature() {
            private String output;
            private String variable;

            @Override
            public void configure(ConfigurationContext configurationContext) {
                configurationContext
                        .addConfigurationParameter(ConfigurationParameter.String("variable",
                                                                                 "The variable to modify",
                                                                                 true,
                                                                                 null,
                                                                                 value -> variable = value));
                configurationContext
                        .addConfigurationParameter(ConfigurationParameter.String("output",
                                                                                 "The expected output",
                                                                                 true,
                                                                                 null,
                                                                                 value -> output = value));
            }

            @Override
            public Map<String, Supplier<List<String>>> processApplicationArguments(
                    Map<String, Supplier<List<String>>> applicationArguments) {
                Map<String, Supplier<List<String>>> result = new HashMap<>(applicationArguments);
                result.put(variable, () -> Stream.of(output).collect(Collectors.toList()));
                return result;
            }
        };

        configure(applicationConfigurationContext -> applicationConfigurationContext
                .addConfigurationParameter(ConfigurationParameter.String("test",
                                                                         "The test value",
                                                                         true,
                                                                         null,
                                                                         result::set)));

        ApplicationStatusListener listener = new ApplicationStatusListener() {
            @Override
            public void onStartupFinished() {
                assertEquals("reset", result.get());
            }
        };
        String[] arguments = {"--variable=test", "--output=reset", "--test=set"};
        ApplicationContainer.start(application, arguments, listener, feature);
    }

    private void configure(Consumer<ApplicationConfigurationContext> configuration) {
        doAnswer(invocation -> {
            configuration.accept((ApplicationConfigurationContext) invocation.getArguments()[0]);
            return null;
        }).when(application).configure(any());
    }
}