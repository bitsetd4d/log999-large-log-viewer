package com.log999.logfile.loglineprovider;

import com.log999.loading.api.StreamingLogLineProvider;
import com.log999.loading.internal.InMemoryLogLineProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InMemoryLogLineProviderTest {

    @Mock
    private StreamingLogLineProvider streamingLogLineProvider;

    @Before
    public void setUp() throws Exception {
        when(streamingLogLineProvider.readLine())
                .thenReturn("LINE1")
                .thenReturn("LINE2")
                .thenReturn("LINE3")
                .thenReturn(null);
    }

    @Test(timeout = 500L)
    public void loadLinesFromStream() throws Exception {
        // Given
        InMemoryLogLineProvider logLineProvider = new InMemoryLogLineProvider(streamingLogLineProvider);
        // When
        String line1 = logLineProvider.getLine(0);
        String line2 = logLineProvider.getLine(1);
        String line3 = logLineProvider.getLine(2);
        String line4 = logLineProvider.getLine(3);
        // Then
        assertThat(line1, equalTo("LINE1"));
        assertThat(line3, equalTo("LINE3"));
        assertThat(line2, equalTo("LINE2"));
        assertThat(line4, is(nullValue()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void linesLessThanZeroAreNotAllowed() throws Exception {
        InMemoryLogLineProvider logLineProvider = new InMemoryLogLineProvider(streamingLogLineProvider);
        logLineProvider.getLine(-1);
    }
}