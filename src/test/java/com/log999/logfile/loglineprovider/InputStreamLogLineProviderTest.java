package com.log999.logfile.loglineprovider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class InputStreamLogLineProviderTest {

    private InputStream inputStream;
    private InputStreamLogLineProvider providerUnderTest;

    @Before
    public void setUp() throws Exception {
        BufferedInputStream rawInputStream = new BufferedInputStream(new ByteArrayInputStream("LINE1\nLINE2\nLINE3\n".getBytes()));
        inputStream = spy(rawInputStream);
        providerUnderTest = new InputStreamLogLineProvider(inputStream);
    }

    @Test
    public void readingGivesCorrectResults() throws Exception {
        assertThat(providerUnderTest.readLine(), equalTo("LINE1"));
        assertThat(providerUnderTest.readLine(), equalTo("LINE2"));
        assertThat(providerUnderTest.readLine(), equalTo("LINE3"));
        String actual = providerUnderTest.readLine();
        assertThat(actual, is(nullValue()));
    }

    @Test(timeout = 100L)
    public void readingFullyClosesStream() throws Exception {
        verify(inputStream, times(0)).close();
        while ((providerUnderTest.readLine()) != null);
        verify(inputStream, times(1)).close();
    }

    @Test(timeout = 100L)
    public void shouldSetHasMoreLinesIfDataAvailable() throws Exception {
        assertThat(providerUnderTest.hasMoreLines(), equalTo(true));
        while (providerUnderTest.hasMoreLines()) {
            String line = providerUnderTest.readLine();
            assertThat(line, not(nullValue()));
        }
        assertThat(providerUnderTest.hasMoreLines(), equalTo(false));
        assertThat(providerUnderTest.readLine(), nullValue());
    }
}