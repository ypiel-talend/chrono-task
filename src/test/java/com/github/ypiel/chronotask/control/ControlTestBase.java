package com.github.ypiel.chronotask.control;

import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public abstract class ControlTestBase {

    @BeforeAll
    public static void initJfx() {
        try {
            Platform.startup(() -> {
            });
        } catch (IllegalStateException e) {
            // Toolkit already initialized
        }
    }

    protected void runOnJfxThread(Runnable action) throws InterruptedException {
        if (Platform.isFxApplicationThread()) {
            action.run();
        } else {
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                try {
                    action.run();
                } finally {
                    latch.countDown();
                }
            });
            if (!latch.await(5, TimeUnit.SECONDS)) {
                throw new RuntimeException("Timeout waiting for FX Thread");
            }
        }
    }
}
