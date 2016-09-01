package com.blinglog.poc.file;

import com.log999.markup.LineMarkup;
import javafx.scene.paint.Color;

public interface LogFileLine {

    long getLineNumber();

    int getDisplayRowCount();
    LogFileDisplayRow[] getDisplayRows();

    boolean isMarked();
    void setMarked(boolean marked);

    LineMarkup getLineMarkup();

    void markBold(int start, int end);
    void markBackground(int start, int end, Color bg);
    void markForeground(int start, int end, Color bg);

}
