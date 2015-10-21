package com.blinglog.poc.file;

import com.blinglog.poc.control.internal.DisplayProperties;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public interface LogFileDisplayRow {

    String getText();

    void markBold(int start, int end);
    void markSelectedBackground(int start, int end, Color bg);
    void markSelectedForeground(int start, int end, Color fg);

    void drawText(GraphicsContext gc, double x, double y, DisplayProperties displayProperties);

    void dumpToLog(int i);
}
