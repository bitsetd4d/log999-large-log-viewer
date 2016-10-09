package com.log999.display.internal;

import com.log999.display.api.LogFileDisplayRow;
import com.log999.display.api.LogFileLine;
import com.log999.markup.MarkupMemory;
import com.log999.util.LogFilePosition;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class LogFilePageImplTest {

    @Mock
    private MarkupMemory markupMemory;

    @Test
    public void isHoldingPage() throws Exception {
        LogFilePageImpl holdingPage = testPageBuilder().setHoldingPage(true).createLogFilePageImpl();
        LogFilePageImpl nonHoldingPage = testPageBuilder().setHoldingPage(false).createLogFilePageImpl();

        assertTrue(holdingPage.isHoldingPage());
        assertFalse(nonHoldingPage.isHoldingPage());
    }

    private LogFilePageImplBuilder testPageBuilder() {
        return new LogFilePageImplBuilder()
                .setTopDisplayRow(0)
                .setDisplayRowsToFill(99)
                .setRows(new String[]{"ROW1", "ROW2"})
                .setHoldingPage(false)
                .setLineWrapLength(999)
                .setPositionTopLine(new LogFilePosition(0, 0))
                .setMarkupMemory(markupMemory);
    }

    @Test
    public void getTopDisplayRow() throws Exception {
        long expectedDisplayRow = 123;
        LogFilePageImpl page = testPageBuilder().setTopDisplayRow(expectedDisplayRow).createLogFilePageImpl();
        assertThat(page.getTopDisplayRow(), equalTo(expectedDisplayRow));
    }

    @Test
    public void getRowCount() throws Exception {
        String[] rows = new String[] { "A", "B", "C" };
        LogFilePageImpl page = testPageBuilder().setRows(rows).createLogFilePageImpl();
        assertThat(page.getRowCount(), equalTo(rows.length));
    }

    @Test
    public void getFirstLineOffset() throws Exception {
        int expectedOffset = 1;
        LogFilePageImpl page = testPageBuilder()
                .setPositionTopLine(new LogFilePosition(0, expectedOffset))
                .createLogFilePageImpl();
        assertThat(page.getFirstLineOffset(), equalTo(expectedOffset));
    }

    @Test
    public void getRow() throws Exception {
        String[] rows = new String[] { "A", "B", "C" };
        LogFilePageImpl page = testPageBuilder().setRows(rows).createLogFilePageImpl();
        LogFileLine row = page.getRow(0);
        assertThat(row, notNullValue());
    }

    @Test
    public void getDisplayRowCount() throws Exception {
        String longStringValue = StringUtils.repeat("X ", 100);
        int wordWrap = (int)(longStringValue.length() * 0.6);
        String[] rows = new String[] { longStringValue, longStringValue, longStringValue };
        LogFilePageImpl page = testPageBuilder().setRows(rows).setLineWrapLength(wordWrap).createLogFilePageImpl();
        assertThat(page.getDisplayRowCount(), equalTo(rows.length * 2));
    }

    @Test
    public void getDisplayRow() throws Exception {
        String row1 = StringUtils.repeat("A ", 100);
        String row2 = StringUtils.repeat("B ", 100);
        String row3 = StringUtils.repeat("C ", 100);
        int wordWrap = (int)(row1.length() * 0.6);
        String[] rows = new String[] { row1, row2, row3 };
        LogFilePageImpl page = testPageBuilder().setRows(rows).setLineWrapLength(wordWrap).createLogFilePageImpl();

        assertThat(page.getDisplayRow(0).getText(), startsWith("A"));
        assertThat(page.getDisplayRow(1).getText(), startsWith("A"));

        assertThat(page.getDisplayRow(2).getText(), startsWith("B"));
        assertThat(page.getDisplayRow(3).getText(), startsWith("B"));

        assertThat(page.getDisplayRow(4).getText(), startsWith("C"));
        assertThat(page.getDisplayRow(5).getText(), startsWith("C"));
    }

    @Test
    public void getLogFileLineForDisplayRow() throws Exception {
        LogFilePageImpl page = testPageBuilder().createLogFilePageImpl();
        LogFileLine logFileLineForDisplayRow = page.getLogFileLineForDisplayRow(0);
        LogFileDisplayRow[] displayRows = logFileLineForDisplayRow.getDisplayRows();
        assertThat(displayRows.length, is(equalTo(1)));
    }

}