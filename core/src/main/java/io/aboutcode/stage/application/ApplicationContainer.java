package io.aboutcode.stage.application;

import io.aboutcode.stage.component.Component;
import io.aboutcode.stage.component.ComponentBundle;
import io.aboutcode.stage.component.ComponentContainer;
import io.aboutcode.stage.concurrent.SignalCondition;
import io.aboutcode.stage.configuration.ApplicationArgumentParser;
import io.aboutcode.stage.configuration.ApplicationConfigurationContext;
import io.aboutcode.stage.configuration.ArgumentParseException;
import io.aboutcode.stage.configuration.ConfigurationParameter;
import io.aboutcode.stage.configuration.ParameterParser;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
public class ApplicationContainer {
    private static final int EXIT_NORMAL = 0;
    private static final int EXIT_ERROR = -1;
    private final List<ConfigurationParameter> configurationParameters = new ArrayList<>();
    private final SignalCondition requireShutdown = new SignalCondition();
    private final ComponentContainer componentContainer = new ComponentContainer(
            "MainComponentContainer", requireShutdown::signalAll);
    private Map<String, List<String>> applicationArguments;
    private String[] arguments;
    private boolean daemonize;
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
     */
    @SuppressWarnings("unused")
    public static void start(Application application, String[] arguments) {
        start(application, arguments, null);
    }

    /**
     * Runs the specified application with the specified arguments as a one-off run. I.e. after the
     * method {@link ComponentContainer#start()} has been called, {@link ComponentContainer#stop()}
     * will be called immediately afterwards.
     *
     * @param application               The application to run
     * @param arguments                 The arguments to configure the application with
     * @param applicationStatusListener The listener that should be notified for application events
     */
    @SuppressWarnings({"unused", "WeakerAccess"})
    public static void start(Application application, String[] arguments,
                             ApplicationStatusListener applicationStatusListener) {
        start(application, arguments, applicationStatusListener, false);
    }

    /**
     * Runs the specified application with the specified arguments as a deamon. I.e. after the
     * method {@link ComponentContainer#start()} has been called, the application will wait for an
     * explicit call to {@link ApplicationContainer#shutdown(String)}.
     *
     * @param application               The application to run
     * @param arguments                 The arguments to configure the application with
     * @param applicationStatusListener The listener that should be notified for application events
     */
    @SuppressWarnings({"unused", "WeakerAccess"})
    public static void startDaemon(Application application, String[] arguments,
                                   ApplicationStatusListener applicationStatusListener) {
        start(application, arguments, applicationStatusListener, true);
    }

    /**
     * Runs the specified application with the specified arguments as a deamon. I.e. after the
     * method {@link ComponentContainer#start()} has been called, the application will wait for an
     * explicit call to {@link ApplicationContainer#shutdown(String)}.
     *
     * @param application The application to run
     * @param arguments   The arguments to configure the application with
     */
    @SuppressWarnings("unused")
    public static void startDaemon(Application application, String[] arguments) {
        startDaemon(application, arguments, null);
    }

    private static void start(Application application, String[] arguments,
                              ApplicationStatusListener applicationStatusListener, boolean daemon) {
        new ApplicationContainer().run(application, applicationStatusListener, arguments, daemon);
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
            logger.info("Shutting down: " + message);
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
                     String[] arguments, boolean daemon) {
        this.applicationStatusListener = applicationStatusListener;
        this.daemonize = daemon;
        this.arguments = arguments;
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
        configurationParameters.add(ConfigurationParameter.File("configuration-file",
                                                                "The file containing configuration parameters that are used in addition to any command line parameters",
                                                                false, true, file -> {
                    List<String> fileContents;
                    try {
                        fileContents = Files.lines(file.toPath())
                                            .map(line -> "--" + line)
                                            .collect(Collectors.toList());
                    } catch (IOException e) {
                        throw new IllegalStateException(String
                                                                .format("Could not load file '%s' because: %s",
                                                                        file.getAbsolutePath(),
                                                                        e.getMessage()), e);
                    }

                    Map<String, List<String>> configurationProperties;
                    try {
                        configurationProperties = ApplicationArgumentParser
                                .parseArguments(fileContents.toArray(new String[0]));
                    } catch (ArgumentParseException e) {
                        throw new IllegalStateException(String
                                                                .format(
                                                                        "Could not parse file contents in file '%s' because: %s",
                                                                        file.getAbsolutePath(),
                                                                        e.getMessage()), e);
                    }

                    // add the loaded parameters to the existing parameters if they do not exist already
                    for (Map.Entry<String, List<String>> entry : configurationProperties
                            .entrySet()) {
                        String key = entry.getKey();
                        List<String> values = entry.getValue();
                        if (!applicationArguments.containsKey(key)) {
                            applicationArguments.put(key, values);
                        }
                    }
                }
        ));
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

    private int doRun(Application application) {
        notifyStartupEvent("Startup", 10, 0, "Configuring component bundle");
        final List<ComponentBundle> componentBundles = new ArrayList<>();
        application.configure(new DefaultApplicationConfigurationContext(
                0,
                componentBundles,
                configurationParameters
        ));

        // use component bundle
        notifyStartupEvent("Startup", 10, 1, "Configuring component bundle");
        final List<ComponentBundle> allComponentBundles = new ArrayList<>();
        while (!componentBundles.isEmpty()) {
            final List<ComponentBundle> newBundles = new ArrayList<>();
            for (ComponentBundle bundle : componentBundles) {
                bundle.configure(new DefaultApplicationConfigurationContext(1,
                                                                            newBundles,
                                                                            configurationParameters));
            }

            allComponentBundles.addAll(componentBundles);
            componentBundles.clear();
            componentBundles.addAll(newBundles);
        }

        notifyStartupEvent("Startup", 10, 2, "Parsing application arguments");
        try {
            applicationArguments = ApplicationArgumentParser.parseArguments(arguments);
        } catch (ArgumentParseException e) {
            logger.error(
                    String.format("Could not parse application arguments because: %s",
                                  e.getMessage()), e);
            return EXIT_ERROR;
        }

        // find special argument that contains configuration parameters in file format
        notifyStartupEvent("Startup", 10, 3, "Processing additional application arguments file");

        // now apply all parameters
        notifyStartupEvent("Startup", 10, 4, "Applying configuration parameters");
        List<String> unusedParameters = new ArrayList<>(applicationArguments.keySet());
        List<ConfigurationParameter> missingParameters = new ArrayList<>();
        for (ConfigurationParameter configurationParameter : configurationParameters) {
            String name = configurationParameter.getName();
            if (!applicationArguments.containsKey(name) && configurationParameter.isMandatory()) {
                missingParameters.add(configurationParameter);
            } else {
                List<String> values = applicationArguments.get(name);
                configurationParameter.apply(applicationArguments.containsKey(name), values);
            }
            unusedParameters.remove(name);
        }

        List<ConfigurationParameter> stillMissingParameters = new ArrayList<>(missingParameters);
        for (ConfigurationParameter configurationParameter : stillMissingParameters) {
            String name = configurationParameter.getName();
            if (applicationArguments.containsKey(name)) {
                List<String> values = applicationArguments.get(name);
                configurationParameter.apply(applicationArguments.containsKey(name), values);
                missingParameters.remove(configurationParameter);
            }
        }

        notifyStartupEvent("Startup", 10, 6, "Checking for unused configuration parameters");
        if (unusedParameters.size() > 0) {
            requireShutdown.signalAll();
            for (String unusedParameter : unusedParameters) {
                logger.warn(String.format("Unused argument found: %s", unusedParameter));
            }
        }

        notifyStartupEvent("Startup", 10, 7, "Checking for missing configuration parameters");
        if (missingParameters.size() > 0) {
            requireShutdown.signalAll();
            for (ConfigurationParameter missingParameter : missingParameters) {
                logger.error(String.format("Missing argument: %s - %s", missingParameter.getName(),
                                           missingParameter.getDescription()));
            }
        }

        if (requireShutdown.isSignalled()) {
            return EXIT_ERROR;
        }

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
        allComponentBundles.forEach(bundle -> bundle.assemble(applicationAssemblyContext));
        application.assemble(applicationAssemblyContext);

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

    private class DefaultApplicationConfigurationContext implements
            ApplicationConfigurationContext {
        private final int processedActions;
        private final List<ComponentBundle> componentBundles;
        private final List<ConfigurationParameter> configurationParameters;

        private DefaultApplicationConfigurationContext(final int processedActions,
                                                       final List<ComponentBundle> componentBundles,
                                                       final List<ConfigurationParameter> configurationParameters) {
            this.processedActions = processedActions;
            this.componentBundles = componentBundles;
            this.configurationParameters = configurationParameters;
        }

        @Override
        public void addComponentBundle(ComponentBundle componentBundle) {
            notifyStartupEvent("Startup", 10, processedActions, String
                    .format("Adding bundle '%s'", componentBundle.getClass().getSimpleName()));
            componentBundles.add(componentBundle);
        }

        @Override
        public void addConfigurationParameter(
                ConfigurationParameter configurationParameter) {
            notifyStartupEvent("Startup", 10, processedActions, String
                    .format("Adding configuration parameter '%s'",
                            configurationParameter.getName()));
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