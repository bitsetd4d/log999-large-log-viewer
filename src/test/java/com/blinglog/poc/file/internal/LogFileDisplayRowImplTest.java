package com.blinglog.poc.file.internal;

import com.log999.display.api.LogFileLine;
import javafx.scene.paint.Color;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class LogFileDisplayRowImplTest {

    private LogFileDisplayRowImpl underTest;

    @Mock
    private LogFileLine logFileLine;

    @Before
    public void setUp() throws Exception {
        underTest = new LogFileDisplayRowImpl(logFileLine, 10, "DISPLAY1", 0);
    }

    @Test
    public void getText() throws Exception {
        assertThat(underTest.getText(), equalTo("DISPLAY1"));
    }

    @Test
    public void markBold() throws Exception {
        // When
        underTest.markBold(0, 10, true);
        // Then
        verify(logFileLine).markBold(10, 20, true);
    }

    @Test
    public void markBackground() throws Exception {
        // When
        underTest.markBackground(0, 10, Color.AQUA);
        // Then
        verify(logFileLine).markBackground(10, 20, Color.AQUA);
    }

    @Test
    public void markForeground() throws Exception {
        // When
        underTest.markForeground(0, 10, Color.GREEN);
        // Then
        verify(logFileLine).markForeground(10, 20, Color.GREEN);
    }

}