package com.blinglog.poc.control.internal;

import com.blinglog.poc.file.LogFileAccess;
import com.blinglog.poc.file.LogFileDisplayRow;
import com.blinglog.poc.file.LogFilePage;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.Shadow;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Display text of the log file page.
 */
public class LogTextCanvas extends Canvas {

    private IntegerProperty topIndexProperty = new SimpleIntegerProperty();
    private IntegerProperty leftIndexProperty = new SimpleIntegerProperty();

    private LogFileAccess logFileAccess;
    private DisplayProperties displayProperties;

    public LogTextCanvas() {
        topIndexProperty.addListener(evt -> requestData());
        leftIndexProperty.addListener(evt -> draw());
        widthProperty().addListener(evt -> requestData());
        heightProperty().addListener(evt -> requestData());
    }

    public IntegerProperty topIndexProperty() { return topIndexProperty; }
    public IntegerProperty leftIndexProperty() { return leftIndexProperty; }

    public void set(LogFileAccess logFileAccess,DisplayProperties displayProperties) {
        this.displayProperties = displayProperties;
        this.logFileAccess = logFileAccess;
        logFileAccess.logFilePageProperty().addListener(ev -> draw());
        logFileAccess.fullyIndexedProperty().addListener(evt -> requestData());
        displayProperties.fontProperty().addListener(ev -> requestData());
    }

    private void requestData() {
        double height = getHeight();
        int rows = 1 + (int)(height / displayProperties.lineHeightProperty().get());
        logFileAccess.setRangeOfInterest(topIndexProperty.get(),rows);
    }

    public void draw() {
        double width = getWidth();
        double height = getHeight();
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, width, height);

        LogFilePage logFilePage = logFileAccess.logFilePageProperty().get();
        if (logFilePage == null) return;

        gc.setFont(displayProperties.fontProperty().get());
        if (logFilePage.isHoldingPage()) {
            gc.setEffect(new Shadow(BlurType.ONE_PASS_BOX, Color.BLACK, 12));
        }

        double leftX = -leftIndexProperty.get() * displayProperties.charWidthProperty().get();
        int count = logFilePage.getDisplayRowCount();
        double y = displayProperties.lineHeightProperty().get();
        for (int i=0; i<count; i++) {
            LogFileDisplayRow row = logFilePage.getDisplayRow(i);
            row.drawText(gc,leftX,y,displayProperties);
            y += displayProperties.lineHeightProperty().get();
        }
        gc.setEffect(null);
    }
    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    public double prefWidth(double height) {
        return getWidth();
    }

    @Override
    public double prefHeight(double width) {
        return getHeight();
    }


}

