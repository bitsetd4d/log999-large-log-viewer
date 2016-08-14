package com.log999.task;


import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class TestTasks {

    private TaskRunner taskRunner;
    private TaskRunner.TaskFeedback taskFeedback;

    @BeforeClass
    public static void setUpJavaFx() throws Exception {
        JavaFXTestUtil.setupJavaFX();
    }

    @Before
    public void setUp() throws Exception {
        taskRunner = new TaskRunner();
        taskFeedback = new TaskRunner.TaskFeedback() {
            @Override
            public void setVisible(boolean visible) {

            }

            @Override
            public void setText(String text) {

            }
        };
        taskRunner.setTaskFeedback(taskFeedback);
    }

    @Test
    public void testSimpleExecution() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        taskRunner.execute("Testing", () -> latch.countDown());
        boolean ok = latch.await(1, TimeUnit.SECONDS);
        assertThat(ok, is(equalTo(true)));
    }

    // TODO http://stackoverflow.com/questions/28245555/how-do-you-mock-a-javafx-toolkit-initialization
}
