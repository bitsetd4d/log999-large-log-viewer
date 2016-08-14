package com.log999.task;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;

import javax.swing.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;

class JavaFXTestUtil {

    private static final CountDownLatch latch = new CountDownLatch(1);

    synchronized  static void setupJavaFX() throws InterruptedException {
        if (latch.getCount() == 0) return;

        long timeMillis = System.currentTimeMillis();

        SwingUtilities.invokeLater(() -> {
            new JFXPanel();
            latch.countDown();
        });

        System.out.println("javafx initialising...");
        latch.await();
        System.out.println("javafx is initialised in " + (System.currentTimeMillis() - timeMillis) + "ms");
    }

    static void waitForJavaFxToCatchup() throws InterruptedException {
        CountDownLatch waitLatch = new CountDownLatch(1);
        Platform.runLater(waitLatch::countDown);
        boolean ok = waitLatch.await(5, SECONDS);
        if (!ok) {
            throw new IllegalStateException("JavaFX didn't catch up");
        }
    }
}
