package io.aboutcode.stage.application;

/**
 * Implementors of this interface will be notified of events generated during the lifecycle of an
 * {@link Application}. This can be used to show a graphical interface with a "loading" animation,
 * for example.
 */
public interface ApplicationStatusListener {
    /**
     * Called for every phase of the startup procedure.
     *
     * @param phase                       The current phase the application is in
     * @param estimatedStartupActionCount The estimated amount of actions the startup entails
     * @param processedStartupActionCount The amount of actions already processed during startup
     * @param eventInformation            Additional information on the event
     */
    void onStartupEvent(String phase,
                        int estimatedStartupActionCount,
                        int processedStartupActionCount,
                        String eventInformation);

    /**
     * Called when the startup procedure has concluded.
     */
    void onStartupFinished();

    /**
     * Called for every phase of the shutdown procedure.
     *
     * @param phase                        The current phase the application is in
     * @param estimatedShutdownActionCount The estimated amount of actions the shutdown entails
     * @param processedShutdownActionCount The amount of actions already processed during shutdown
     * @param eventInformation             Additional information on the event
     */
    void onShutdownEvent(String phase,
                         int estimatedShutdownActionCount,
                         int processedShutdownActionCount,
                         String eventInformation);

    /**
     * Called when the shutdown procedure has concluded.
     */
    void onShutdownFinished();
}
