package com.log999.display.api;

import com.blinglog.poc.control.internal.DisplayProperties;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public interface LogFileDisplayRow {

    String getText();

    void markBold(int start, int end, boolean bold);
    void markBackground(int start, int end, Color bg);
    void markForeground(int start, int end, Color fg);

    void drawText(GraphicsContext gc, double x, double y, DisplayProperties displayProperties);

    void dumpToLog(int i);
}
