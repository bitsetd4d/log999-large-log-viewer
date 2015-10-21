package com.blinglog.poc.file;

/**
 *
 */
public interface LogFilePage {

    boolean isHoldingPage();

    long getTopDisplayRow();
    int getRowCount();
    int getFirstLineOffset();
    LogFileLine getRow(int index);

    int getDisplayRowCount();
    LogFileDisplayRow getDisplayRow(int displayRowIndex);
    LogFileLine getLogFileLineForDisplayRow(int displayRowIndex);

}
