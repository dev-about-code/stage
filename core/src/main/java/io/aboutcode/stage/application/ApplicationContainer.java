package io.aboutcode.stage.application;

import io.aboutcode.stage.component.Component;
import io.aboutcode.stage.component.ComponentContainer;
import io.aboutcode.stage.concurrent.SignalCondition;
import io.aboutcode.stage.configuration.ApplicationArgumentParser;
import io.aboutcode.stage.configuration.ArgumentParseException;
import io.aboutcode.stage.configuration.ConfigurationContext;
import io.aboutcode.stage.configuration.ConfigurationParameter;
import io.aboutcode.stage.configuration.ParameterParser;
import io.aboutcode.stage.feature.Feature;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>This is the main container of any application that is run through the stage framework.
 * It takes care of creating the main component container as well as transitioning through the
 * stages of the application.</p>
 *
 * <p>The container allows running an application in two ways: as a deamon that keeps running until
 * explicitly shut down or as a one-off run.</p>
 */
// TODO: this class needs cleaning up
// TODO: make this testable (no System.exit())
public class ApplicationContainer {
    private static final int EXIT_NORMAL = 0;
    private static final int EXIT_ERROR = -1;
    private final SignalCondition requireShutdown = new SignalCondition();
    private final ComponentContainer componentContainer = new ComponentContainer(
            "MainComponentContainer", requireShutdown::signalAll);
    private List<ConfigurationParameter> configurationParameters = new ArrayList<>();
    private String[] arguments;
    private boolean daemonize;
    private List<Feature> features;
    private Thread runner;
    private Logger logger;
    private ApplicationStatusListener applicationStatusListener;

    /**
     * Runs the specified application with the specified arguments as a one-off run. I.e. after the
     * method {@link ComponentContainer#start()} has been called, {@link ComponentContainer#stop()}
     * will be called immediately afterwards.
     *
     * @param application The application to run
     * @param arguments   The arguments to configure the application with
     * @param features    The features to use for this application
     */
    @SuppressWarnings("unused")
    public static void start(Application application, String[] arguments, Feature... features) {
        start(application, arguments, null, features);
    }

    /**
     * Runs the specified application with the specified arguments as a one-off run. I.e. after the
     * method {@link ComponentContainer#start()} has been called, {@link ComponentContainer#stop()}
     * will be called immediately afterwards.
     *
     * @param application               The application to run
     * @param arguments                 The arguments to configure the application with
     * @param applicationStatusListener The listener that should be notified for application events
     * @param features                  The features to use for this application
     */
    @SuppressWarnings({"unused", "WeakerAccess"})
    public static void start(Application application, String[] arguments,
                             ApplicationStatusListener applicationStatusListener,
                             Feature... features) {
        start(application, arguments, applicationStatusListener, false, features);
    }

    /**
     * Runs the specified application with the specified arguments as a deamon. I.e. after the
     * method {@link ComponentContainer#start()} has been called, the application will wait for an
     * explicit call to {@link ApplicationContainer#shutdown(String)}.
     *
     * @param application               The application to run
     * @param arguments                 The arguments to configure the application with
     * @param applicationStatusListener The listener that should be notified for application events
     * @param features                  The features to use for this application
     */
    @SuppressWarnings({"unused", "WeakerAccess"})
    public static void startDaemon(Application application, String[] arguments,
                                   ApplicationStatusListener applicationStatusListener,
                                   Feature... features) {
        start(application, arguments, applicationStatusListener, true, features);
    }

    /**
     * Runs the specified application with the specified arguments as a deamon. I.e. after the
     * method {@link ComponentContainer#start()} has been called, the application will wait for an
     * explicit call to {@link ApplicationContainer#shutdown(String)}.
     *
     * @param application The application to run
     * @param arguments   The arguments to configure the application with
     * @param features    The features to use for this application
     */
    @SuppressWarnings("unused")
    public static void startDaemon(Application application, String[] arguments,
                                   Feature... features) {
        startDaemon(application, arguments, null, features);
    }

    private static void start(Application application, String[] arguments,
                              ApplicationStatusListener applicationStatusListener, boolean daemon,
                              Feature... features) {
        new ApplicationContainer()
                .run(application, applicationStatusListener, arguments, daemon, features);
    }

    private static List<ConfigurationParameter> configureFeatures(List<Feature> features) {
        final List<ConfigurationParameter> configurationParameters = new ArrayList<>();
        ConfigurationContext configurationContext = new DefaultConfigurationContext(
                configurationParameters);
        features.forEach(feature -> feature.configure(configurationContext));
        return configurationParameters;
    }

    private static void applyConfiguration(
            List<ConfigurationParameter> configurationParameters,
            Map<String, Supplier<List<String>>> applicationArguments) {
        for (ConfigurationParameter configurationParameter : configurationParameters) {
            String name = configurationParameter.getName();
            if (applicationArguments.containsKey(name)) {
                List<String> values = applicationArguments.get(name).get();
                configurationParameter.apply(applicationArguments.containsKey(name), values);
            }
        }
    }

    /**
     * Informes the application that it shut down, stopping the internal {@link ComponentContainer}
     * in turn as well. If the application or its internal container is already shut down, this does
     * nothing.
     *
     * @param message The message to log as the reason for the shut down
     */
    @SuppressWarnings("WeakerAccess")
    public void shutdown(String message) {
        if (componentContainer.isRunning()) {
            notifyShutdownEvent("Shutdown", 2, 0, "Shutting down application");
            logger.info("Shutting down: {}", message);
            if (runner != null && runner != Thread.currentThread()) {
                runner.interrupt();
                try {
                    runner.join(1000L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            notifyShutdownEvent("Shutdown", 2, 1, "Stopping container and components");
            componentContainer.stop();

            notifyShutdownEvent("Shutdown", 2, 2, "Shutdown complete");
            logger.info("Shutdown completed");
        }
    }

    private void run(Application application, ApplicationStatusListener applicationStatusListener,
                     String[] arguments, boolean daemon, Feature... features) {
        this.applicationStatusListener = applicationStatusListener;
        this.daemonize = daemon;
        this.arguments = arguments;
        this.features = features == null ? Collections.emptyList() :
                        Stream.of(features).collect(Collectors.toList());
        init(application);
        System.exit(doRun(application));
    }

    private void notifyStartupEvent(String phase, int estimatedActions, int processedActions,
                                    String eventInformation) {
        if (applicationStatusListener != null) {
            applicationStatusListener
                    .onStartupEvent(phase, estimatedActions, processedActions, eventInformation);
        }
    }

    private void notifyShutdownEvent(String phase, int estimatedActions, int processedActions,
                                     String eventInformation) {
        if (applicationStatusListener != null) {
            applicationStatusListener
                    .onShutdownEvent(phase, estimatedActions, processedActions, eventInformation);
        }
    }

    private void init(Application application) {
        // all standard components and other default configurations go here
        notifyStartupEvent("Initialization", 2, 0, "Initializing logging engine");
        logger = LoggerFactory.getLogger(application.getClass());

        // allow printing usage
        notifyStartupEvent("Initialization", 2, 1, "Configuring extra application parameters");
        configurationParameters.add(ConfigurationParameter
                                            .Option("help",
                                                    "If set, the application will print its parameters and then quit",
                                                    p -> {
                                                        if (p) {
                                                            printUsage();
                                                            requireShutdown.signalAll();
                                                        }
                                                    }
                                            ));
        notifyStartupEvent("Initialization", 2, 2, "Initialization finished");
    }

    private void printUsage() {
        List<ConfigurationParameter> parameters = new ArrayList<>(configurationParameters);
        int maxNameLength = parameters.stream().map(parameter -> parameter.getName().length())
                                      .max(Comparator.naturalOrder()).orElse(0);
        int maxTypeLength = parameters.stream().map(parameter -> parameter.getTypeName().length())
                                      .max(Comparator.naturalOrder()).orElse(0);
        StringBuilder usageInformation = new StringBuilder("Usage information:\n");
        for (ConfigurationParameter configurationParameter : parameters.stream()
                                                                       .sorted(Comparator.comparing(
                                                                               ConfigurationParameter::getName))
                                                                       .collect(
                                                                               Collectors
                                                                                       .toList())) {
            usageInformation.append(String
                                            .format(
                                                    "  --%-" + maxNameLength + "s [%-1s] %-" + (
                                                            maxTypeLength
                                                            + 2)
                                                    + "s -> %s\n",
                                                    configurationParameter.getName(),
                                                    configurationParameter.isMandatory() ? "*" : "",
                                                    "[" + configurationParameter.getTypeName()
                                                    + "]",
                                                    configurationParameter.getDescription()));
        }
        logger.info(usageInformation.toString());
    }

    private void printMissing(List<ConfigurationParameter> configurationParameters) {
        for (ConfigurationParameter missingParameter : configurationParameters) {
            logger.error("Missing argument: {} - {}",
                         missingParameter.getName(),
                         missingParameter.getDescription());
        }
    }

    private void printSuperfluous(List<String> arguments) {
        for (String unusedParameter : arguments) {
            logger.warn("Unused argument found: {}", unusedParameter);
        }
    }

    private int doRun(Application application) {
        ApplicationProcessor applicationProcessor = ApplicationProcessor.from(application);

        notifyStartupEvent("Startup", 11, 0, "Configuring component bundles");
        configurationParameters = applicationProcessor.configure();

        notifyStartupEvent("Startup", 10, 1, "Configuring features");
        List<ConfigurationParameter> featureConfigurationParameters = configureFeatures(features);
        this.configurationParameters.addAll(featureConfigurationParameters);

        notifyStartupEvent("Startup", 10, 2, "Parsing application arguments");
        Map<String, Supplier<List<String>>> applicationArguments;
        try {
            applicationArguments = ApplicationArgumentParser.parseArguments(arguments);
        } catch (ArgumentParseException e) {
            requireShutdown.signalAll();
            logger.error("Could not parse application arguments because: {}", e.getMessage(), e);
            return EXIT_ERROR;
        }

        notifyStartupEvent("Startup", 10, 3, "Identifying missing feature parameters");
        ApplicationArgumentMatchResult featureMatchResult = ApplicationArgumentMatchResult
                .between(featureConfigurationParameters, applicationArguments);

        if (featureMatchResult.anyMissing()) {
            requireShutdown.signalAll();
            printMissing(featureMatchResult.getMissing());
            return EXIT_ERROR;
        }

        notifyStartupEvent("Startup", 10, 4, "Applying feature parameters");
        applyConfiguration(featureConfigurationParameters, applicationArguments);

        // modifying application arguments through features
        notifyStartupEvent("Startup", 10, 5, "Processing features");
        if (!features.isEmpty()) {
            for (Feature feature : features) {
                try {
                    applicationArguments = feature
                            .processApplicationArguments(
                                    Collections.unmodifiableMap(applicationArguments));
                } catch (Exception e) {
                    logger.error(
                            "Could not process application arguments in feature {} because: {}",
                            feature.getClass().getSimpleName(),
                            e.getMessage(),
                            e);
                    return EXIT_ERROR;
                }
            }
        }

        // now apply all parameters
        notifyStartupEvent("Startup", 10, 6, "Checking for invalid configurations");
        ApplicationArgumentMatchResult matchResult = ApplicationArgumentMatchResult
                .between(configurationParameters, applicationArguments);

        if (!matchResult.matches()) {
            requireShutdown.signalAll();
            if (matchResult.anyMissing()) {
                printMissing(matchResult.getMissing());
            }

            if (matchResult.anySuperfluous()) {
                printSuperfluous(matchResult.getSuperfluous());
            }
            return EXIT_ERROR;
        }

        notifyStartupEvent("Startup", 10, 7, "Applying application parameters");
        applyConfiguration(configurationParameters, applicationArguments);

        notifyStartupEvent("Startup", 10, 8, "Adding components to container");
        ApplicationAssemblyContext applicationAssemblyContext = new ApplicationAssemblyContext() {
            @Override
            public void addComponent(Object identifier, Component component) {
                notifyStartupEvent("Startup", 10, 8,
                                   String.format("Adding component '%s'", identifier));
                componentContainer.addComponent(identifier, component);
            }

            @Override
            public void addComponent(Component component) {
                this.addComponent(null, component);
            }
        };
        applicationProcessor.assemble(applicationAssemblyContext);

        if (daemonize) {
            runner = Thread.currentThread();
            Runtime.getRuntime().addShutdownHook(
                    new Thread(() -> shutdown("JVM exit"), "Application Cleanup Thread"));
        }

        int exitCode = EXIT_NORMAL;
        try {
            notifyStartupEvent("Startup", 10, 9, "Starting component container");
            componentContainer.start();

            notifyStartupEvent("Startup", 10, 10, "Application started");
            if (applicationStatusListener != null) {
                applicationStatusListener.onStartupFinished();
            }

            if (daemonize && componentContainer.isRunning()) {
                requireShutdown.awaitUninterruptibly();
            }
        } catch (Throwable t) {
            exitCode = EXIT_ERROR;
        } finally {
            if (componentContainer.isRunning()) {
                notifyStartupEvent("Pre shutdown", 2, 0, "Application shutting down");
                logger.info("Application will stop");

                notifyStartupEvent("Pre shutdown", 2, 1, "Stopping component container");
                componentContainer.stop();

                notifyStartupEvent("Pre shutdown", 2, 2, "Application stopped");
                if (applicationStatusListener != null) {
                    applicationStatusListener.onShutdownFinished();
                }
            }
        }

        return exitCode;
    }

    private static class DefaultConfigurationContext implements ConfigurationContext {
        private final List<ConfigurationParameter> configurationParameters;

        private DefaultConfigurationContext(List<ConfigurationParameter> configurationParameters) {
            this.configurationParameters = configurationParameters;
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