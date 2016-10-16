package com.log999.loading.internal;

import com.log999.loading.api.LogLineProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WrappedLogLineProviderImplTest {

    static final String LINE_1 = "Line 1";

    static final String LINE_2A = "0123456789";
    static final String LINE_2B = "ABCDEFGHIJ";
    static final String LINE_2C = "KLM";
    static final String LINE_2 = LINE_2A + LINE_2B + LINE_2C;

    static final String LINE_3 = "";
    static final String LINE_4 = "End";

    @Mock
    private LogLineProvider mockLogLineProvider;

    private WrappedLogLineProviderImpl underTest;

    @Before
    public void setUp() throws Exception {
        when(mockLogLineProvider.getLine(0)).thenReturn(LINE_1);
        when(mockLogLineProvider.getLine(1)).thenReturn(LINE_2);
        when(mockLogLineProvider.getLine(2)).thenReturn(LINE_3);
        when(mockLogLineProvider.getLine(3)).thenReturn(LINE_4);

        underTest = new WrappedLogLineProviderImpl(mockLogLineProvider);
    }

    @Test(timeout = 100L)
    public void unchangedWithLargeLineWrap() throws Exception {
        // When
        underTest.setLineWrap(99);
        // Then
        assertThat(underTest.getDisplayLine(0), equalTo(LINE_1));
        assertThat(underTest.getDisplayLine(1), equalTo(LINE_2));
        assertThat(underTest.getDisplayLine(2), equalTo(LINE_3));
        assertThat(underTest.getDisplayLine(3), equalTo(LINE_4));
    }

    @Test(timeout = 100L)
    public void wrapsWithSmallerLineWrap() throws Exception {
        int wrap = 10;
        // Given
        assertThat(LINE_2.length(), greaterThan(wrap));
        // When
        underTest.setLineWrap(wrap);
        // Then
        assertThat(underTest.getDisplayLine(0), equalTo(LINE_1));
        assertThat(underTest.getDisplayLine(1), equalTo(LINE_2A));
        assertThat(underTest.getDisplayLine(2), equalTo(LINE_2B));
        assertThat(underTest.getDisplayLine(3), equalTo(LINE_2C));
        assertThat(underTest.getDisplayLine(4), equalTo(LINE_3));
        assertThat(underTest.getDisplayLine(5), equalTo(LINE_4));
    }

}