package io.aboutcode.stage.application;

import io.aboutcode.stage.component.ComponentBundle;
import io.aboutcode.stage.configuration.ApplicationConfigurationContext;
import io.aboutcode.stage.configuration.ConfigurationParameter;
import io.aboutcode.stage.configuration.ParameterParser;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

final class ApplicationProcessor {
    private final Application application;
    private final List<ComponentBundle> componentBundles = new ArrayList<>();

    private ApplicationProcessor(Application application) {
        this.application = application;
    }

    public static ApplicationProcessor from(Application application) {
        return new ApplicationProcessor(application);
    }

    List<ConfigurationParameter> configure() {
        RecursiveApplicationConfigurationContext configurationContext = new RecursiveApplicationConfigurationContext();
        application.configure(configurationContext);
        return configurationContext.configurationParameters;
    }

    void assemble(ApplicationAssemblyContext applicationAssemblyContext) {
        componentBundles.forEach(bundle -> bundle.assemble(applicationAssemblyContext));
        application.assemble(applicationAssemblyContext);
    }

    private class RecursiveApplicationConfigurationContext implements
            ApplicationConfigurationContext {
        private final List<ConfigurationParameter> configurationParameters = new ArrayList<>();

        @Override
        public void addComponentBundle(ComponentBundle componentBundle) {
            componentBundles.add(componentBundle);
            componentBundle.configure(this);
        }

        @Override
        public void addConfigurationParameter(ConfigurationParameter configurationParameter) {
            configurationParameters.add(configurationParameter);
        }

        @Override
        public <TypeT> TypeT addConfigurationObject(final String parameterPrefix,
                                                    final TypeT configurationObject) {
            configurationParameters.addAll(
                    ParameterParser.parseParameterClass(parameterPrefix, configurationObject)
            );

            return configurationObject;
        }

        @Override
        public <TypeT> TypeT addConfigurationObject(TypeT configurationObject) {
            return addConfigurationObject(null, configurationObject);
        }
    }
}
