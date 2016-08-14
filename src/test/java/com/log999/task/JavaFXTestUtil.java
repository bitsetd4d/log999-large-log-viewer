package com.log999.task;

import javafx.embed.swing.JFXPanel;

import javax.swing.*;
import java.util.concurrent.CountDownLatch;

public class JavaFXTestUtil {

    private static final CountDownLatch latch = new CountDownLatch(1);

    public synchronized  static void setupJavaFX() throws InterruptedException {
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

}
