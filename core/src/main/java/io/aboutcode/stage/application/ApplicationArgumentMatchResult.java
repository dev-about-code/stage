package io.aboutcode.stage.application;

import io.aboutcode.stage.configuration.ConfigurationParameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

final class ApplicationArgumentMatchResult {
    private final List<String> superfluousKeys;
    private final List<ConfigurationParameter> missingKeys;

    private ApplicationArgumentMatchResult(List<String> superfluousKeys,
                                           List<ConfigurationParameter> missingKeys) {
        this.superfluousKeys = superfluousKeys;

        this.missingKeys = missingKeys;
    }

    static ApplicationArgumentMatchResult between(
            List<ConfigurationParameter> configurationParameters,
            Map<String, Supplier<List<String>>> applicationArguments) {

        List<String> unusedParameters = new ArrayList<>(applicationArguments.keySet());
        List<ConfigurationParameter> missingParameters = new ArrayList<>();

        for (ConfigurationParameter configurationParameter : configurationParameters) {
            String name = configurationParameter.getName();
            if (!applicationArguments.containsKey(name) && configurationParameter.isMandatory()) {
                missingParameters.add(configurationParameter);
            } else {
                List<String> values = applicationArguments.get(name).get();
                configurationParameter.apply(applicationArguments.containsKey(name), values);
            }
            unusedParameters.remove(name);
        }

        List<ConfigurationParameter> stillMissingParameters = new ArrayList<>(missingParameters);
        for (ConfigurationParameter configurationParameter : stillMissingParameters) {
            String name = configurationParameter.getName();
            if (applicationArguments.containsKey(name)) {
                List<String> values = applicationArguments.get(name).get();
                configurationParameter.apply(applicationArguments.containsKey(name), values);
                missingParameters.remove(configurationParameter);
            }
        }

        return new ApplicationArgumentMatchResult(unusedParameters, missingParameters);
    }

    boolean matches() {
        return superfluousKeys.isEmpty() && missingKeys.isEmpty();
    }

    boolean anyMissing() {
        return !missingKeys.isEmpty();
    }

    List<ConfigurationParameter> getMissing() {
        return missingKeys;
    }

    List<String> getSuperfluous() {
        return superfluousKeys;
    }

    boolean anySuperfluous() {
        return !superfluousKeys.isEmpty();
    }
}
