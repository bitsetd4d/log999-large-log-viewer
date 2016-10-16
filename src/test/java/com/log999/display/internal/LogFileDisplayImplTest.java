package com.log999.display.internal;

import com.log999.display.api.LogFileDisplayRow;
import com.log999.display.api.LogFilePage;
import com.log999.loading.api.StreamingLogLineProvider;
import com.log999.markup.MarkupMemory;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Thread.sleep;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class LogFileDisplayImplTest {

    private LogFileDisplayImpl underTest;

    @Mock
    private StreamingLogLineProvider logLineProvider;

    @Mock
    private MarkupMemory markupMemory;

    @Before
    public void setUp() throws Exception {
        underTest = new LogFileDisplayImpl(markupMemory);
        //when(logLineProvider.hasMoreLines()).thenReturn(true, true, true, false);
        when(logLineProvider.readLine()).thenReturn("ROW111", "ROW222", "ROW333X", "ROW4", "ROW5", null);
        underTest.load(logLineProvider);
    }

    @Test(timeout = 100L)
    public void load() throws Exception {
        verify(logLineProvider, atLeast(5)).readLine();
    }

    @Test
    public void getMarkupMemory() throws Exception {
        assertThat(underTest.getMarkupMemory(), is(markupMemory));
    }

    @Test
    public void setLineWrapWidth() throws Exception {

    }

    @Test
    public void setRangeOfInterest() throws Exception {

    }

    @Test
    public void maxLineLengthPropertyIsSet() throws Exception {
        IntegerProperty maxLine = underTest.maxLineLengthProperty();
        assertThat(maxLine.get(), equalTo(7));
    }

    @Test
    public void logFilePageProperty() throws Exception {
        ObjectProperty<LogFilePage> logFilePageObjectProperty = underTest.logFilePageProperty();
        LogFilePage logFilePage = logFilePageObjectProperty.get();
        assertThat(logFilePage, is(nullValue()));

        AtomicReference<LogFilePage> pageHolder = new AtomicReference<>();
        logFilePageObjectProperty.addListener((obs, oldValue, newValue) -> {
            pageHolder.set(newValue);
        });

        // when
        underTest.setRangeOfInterest(2, 2);
        sleep(100);
        // then
        assertThat(pageHolder.get(), is(notNullValue()));
        logFilePage = pageHolder.get();
        assertThat(logFilePage.getDisplayRowCount(), equalTo(2));
        assertThat(logFilePage.getTopDisplayRow(), equalTo(2L));
        assertThat(logFilePage.isHoldingPage(), equalTo(false));

        LogFileDisplayRow displayRow = logFilePage.getDisplayRow(0);
        assertThat(displayRow.getText(), equalTo("ROW333X"));
    }

}