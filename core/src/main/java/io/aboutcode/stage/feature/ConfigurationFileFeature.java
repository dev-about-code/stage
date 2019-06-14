package io.aboutcode.stage.feature;

import io.aboutcode.stage.configuration.ApplicationArgumentParser;
import io.aboutcode.stage.configuration.ArgumentParseException;
import io.aboutcode.stage.configuration.ConfigurationContext;
import io.aboutcode.stage.configuration.ConfigurationParameter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * <p>This feature allows configuration of the application through a dedicated configuration
 * file.</p>
 * <p>The file is expected to follow the {@link java.util.Properties} format and contain one line
 * per argument without leading dashes ('--').</p>
 */
public class ConfigurationFileFeature implements Feature {
    private final boolean fileTakesPrecedence;
    private File configurationFile;

    /**
     * Creates a new feature instance
     *
     * @param fileTakesPrecedence If true, the file takes precendence over command line parameters,
     *                            otherwise command line parameters take precedence
     */
    public ConfigurationFileFeature(boolean fileTakesPrecedence) {
        this.fileTakesPrecedence = fileTakesPrecedence;
    }


    @Override
    public void configure(ConfigurationContext configurationContext) {
        configurationContext
                .addConfigurationParameter(
                        ConfigurationParameter
                                .File("configuration-file",
                                      "The file containing configuration parameters that are used in addition to any command line parameters",
                                      false,
                                      true,
                                      file -> configurationFile = file
                                ));
    }

    @Override
    public Map<String, Supplier<List<String>>> processApplicationArguments(
            Map<String, Supplier<List<String>>> applicationArguments) {
        List<String> fileContents;
        try {
            fileContents = Files.lines(configurationFile.toPath())
                                // the parser expects the dashes
                                .map(line -> "--" + line)
                                .collect(Collectors.toList());
        } catch (IOException e) {
            throw new IllegalStateException(String.format(
                    "Could not load file '%s' because: %s",
                    configurationFile.getAbsolutePath(),
                    e.getMessage()), e);
        }

        Map<String, Supplier<List<String>>> fileConfigurationProperties;
        try {
            fileConfigurationProperties = ApplicationArgumentParser
                    .parseArguments(fileContents.toArray(new String[0]));
        } catch (ArgumentParseException e) {
            throw new IllegalStateException(String.format(
                    "Could not parse file contents in file '%s' because: %s",
                    configurationFile.getAbsolutePath(),
                    e.getMessage()), e);
        }

        // add the loaded parameters to the existing parameters
        Map<String, Supplier<List<String>>> result;
        Map<String, Supplier<List<String>>> supplement;
        if (fileTakesPrecedence) {
            result = new HashMap<>(applicationArguments);
            supplement = fileConfigurationProperties;
        } else {
            result = fileConfigurationProperties;
            supplement = applicationArguments;
        }
        for (Map.Entry<String, Supplier<List<String>>> entry : supplement.entrySet()) {
            String key = entry.getKey();
            Supplier<List<String>> value = entry.getValue();
            if (!result.containsKey(key)) {
                result.put(key, value);
            }
        }

        return result;
    }

}
