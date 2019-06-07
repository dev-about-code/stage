package io.aboutcode.stage.concurrent;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;

public class SignalConditionTest {
    private final ScheduledExecutorService executorService = Executors
            .newSingleThreadScheduledExecutor();
    private SignalCondition signalCondition;

    @Before
    public void setUp() throws Exception {
        signalCondition = new SignalCondition();
    }

    @Test
    public void notification() throws Exception {
        executorService.schedule(() -> signalCondition.signal(), 100, TimeUnit.MILLISECONDS);

        assertTrue(signalCondition.await(200, TimeUnit.MILLISECONDS));
    }

    @Test
    public void postFactum() throws Exception {
        signalCondition.signal();
        assertTrue(signalCondition.await(20, TimeUnit.MILLISECONDS));
    }
}