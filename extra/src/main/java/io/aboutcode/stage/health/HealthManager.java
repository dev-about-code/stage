package io.aboutcode.stage.health;

import io.aboutcode.stage.component.BaseComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * This component will monitor the health of registered {@link HealthMonitorable}s and attempt to
 * revive in case they require it.
 */
public final class HealthManager extends BaseComponent {
    private final ScheduledExecutorService executorService = Executors
            .newSingleThreadScheduledExecutor();
    private final List<ScheduledFuture> schedules = new ArrayList<>();

    /**
     * Adds the specified {@link HealthMonitorable} to the list of monitorables, verifying every
     * <pre>checkInterval</pre>
     * <pre>checkIntervalTimeUnit</pre>s whether the service is still healthy.
     * <p>Note that this immediately starts the interval - only call this method if you expect your
     * service to be healthy already (i.e. after it is started).</p>
     *
     * @param monitorable           The monitorable to add
     * @param checkInterval         The interval to check in
     * @param checkIntervalTimeUnit The unit of the interval
     */
    public void addHealthMonitorable(HealthMonitorable monitorable,
                                     long checkInterval,
                                     TimeUnit checkIntervalTimeUnit) {
        schedules.add(executorService.scheduleWithFixedDelay(() -> {
            if (!monitorable.isHealthy()) {
                monitorable.recover();
            }
        }, checkInterval, checkInterval, checkIntervalTimeUnit));
    }

    @Override
    public void stop() {
        schedules.forEach(scheduledFuture -> scheduledFuture.cancel(true));
    }
}
