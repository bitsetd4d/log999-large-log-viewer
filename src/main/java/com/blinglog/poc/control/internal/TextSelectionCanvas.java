package com.blinglog.poc.control.internal;

import com.blinglog.poc.file.LogFileAccess;
import com.blinglog.poc.file.LogFileDisplayRow;
import com.blinglog.poc.file.LogFilePage;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;


public class TextSelectionCanvas extends Canvas {

    private LogFileAccess logFileAccess;
    private DisplayProperties displayProperties;
    private Font font;
    private float lineHeight;
    private float charWidth;

    private int startx, starty, endx, endy;

    private IntegerProperty topIndexProperty = new SimpleIntegerProperty();
    private IntegerProperty leftIndexProperty = new SimpleIntegerProperty();

    private static boolean DEBUG = true;

    public TextSelectionCanvas() {
        addEventHandler(MouseEvent.MOUSE_PRESSED, this::onPressed);
        addEventHandler(MouseEvent.MOUSE_RELEASED, this::onReleased);
        addEventHandler(MouseEvent.MOUSE_DRAGGED, this::onDragged);
        topIndexProperty.addListener(evt -> draw());
        leftIndexProperty.addListener(evt -> draw());
        widthProperty().addListener(evt -> draw());
    }

    public void set(LogFileAccess logFileAccess, DisplayProperties displayProperties) {
        this.logFileAccess = logFileAccess;
        this.displayProperties = displayProperties;
        updateFromDisplayProperties();
        displayProperties.fontProperty().addListener(ev -> updateFromDisplayProperties());
        displayProperties.lineWrapWidthProperty().addListener(ev -> clearSelection());
        displayProperties.fontProperty().addListener(ev -> draw());
    }

    private void updateFromDisplayProperties() {
        font = displayProperties.fontProperty().get();
        lineHeight = displayProperties.lineHeightProperty().get();
        charWidth = displayProperties.charWidthProperty().get();
    }

    public IntegerProperty topIndexProperty() {
        return topIndexProperty;
    }

    public IntegerProperty leftIndexProperty() {
        return leftIndexProperty;
    }

    private void clearSelection() {
        System.out.println("Clear selection");
        startx = endx = starty = endy = 0;
        draw();
    }

    private void onPressed(MouseEvent ev) {
        double x = ev.getX();
        double y = ev.getY();
        startx = (int) (x / charWidth) + leftIndexProperty.get();
        starty = (int) (y / lineHeight) + topIndexProperty.get();
        endx = startx;
        endy = starty;
        draw();
    }

    private void onReleased(MouseEvent ev) {
        System.out.println("startx=" + startx + ", starty=" + starty + ", endx=" + endx + ", endy=" + endy);
        draw();
    }

    private void onDragged(MouseEvent ev) {
        double x = ev.getX();
        double y = ev.getY();
        endx = (int) (x / charWidth) + leftIndexProperty.get();
        endy = (int) (y / lineHeight) + topIndexProperty.get();
        draw();
    }

    private boolean isForwardsSelection() {
        if (endy > starty) return true;
        if (endy < starty) return false;
        return endx >= startx;
    }

    private void draw() {
        double width = getWidth();
        double height = getHeight();
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, width, height);
        gc.setFill(Color.YELLOW);
        normalizedDraw(gc);
    }

    private LogFileDisplayRow getLine(int row) {
        int idx = row - topIndexProperty.get();
        if (idx < 0) return null;
        LogFilePage logFilePage = logFileAccess.logFilePageProperty().get();
        if (logFilePage != null) {
            return logFilePage.getDisplayRow(idx);
        }
        return null;
    }

    private void normalizedDraw(GraphicsContext gc) {
        int normalisedStartx, normalisedStarty, normalisedEndx, normalisedEndy;
        if (isForwardsSelection()) {
            normalisedStartx = startx;
            normalisedStarty = starty;
            normalisedEndx = endx;
            normalisedEndy = endy;
        } else {
            normalisedStartx = endx;
            normalisedStarty = endy;
            normalisedEndx = startx;
            normalisedEndy = starty;
        }

        double leftX = leftIndexProperty.get() * charWidth;

        if (normalisedStarty == normalisedEndy) {
            double vy = 1 + normalisedStarty * lineHeight;
            double vx1 = normalisedStartx * charWidth;
            double vw = (normalisedEndx - normalisedStartx) * charWidth;
            fillRect(gc, -leftX + vx1, vy, vw, lineHeight);
        } else {
            for (int yy = normalisedStarty; yy <= normalisedEndy; yy++) {
                double vy = 1 + yy * lineHeight;
                if (yy == normalisedStarty) {
                    // First Line
                    double vx1 = normalisedStartx * charWidth;
                    double vw = widthForLine(yy) - vx1;
                    fillRect(gc, -leftX + vx1, vy, vw, lineHeight);
                } else if (yy == normalisedEndy) {
                    // Last Line
                    double vw = normalisedEndx * charWidth;
                    fillRect(gc, -leftX, vy, vw, lineHeight);
                } else {
                    // Middle
                    double vw = widthForLine(yy);
                    fillRect(gc, -leftX, vy, vw, lineHeight);
                }
            }
        }
    }

    private double widthForLine(int row) {
        double lineWidth = 1000;
        LogFileDisplayRow line = getLine(row);
        if (line != null) {
            lineWidth = line.getText().length();
        }
        return lineWidth * charWidth;
    }

    private void fillRect(GraphicsContext gc, double x, double y, double w, double h) {
        //System.out.println("x.y=("+x+","+y+") = w.h("+w+","+h+")");
        double adjustedY = y - topIndexProperty.get() * lineHeight;
        gc.fillRect(x, adjustedY, w, h);
    }

    public void makeSelectedBold() {
        applyToSelection((start, end, line) -> line.markBold(start, end));
    }

    public void makeSelectedBackground(Color bg) {
        applyToSelection((start,end,line) -> line.markSelectedBackground(start, end, bg));
    }

    public void makeSelectedForeground(Color fg) {
        applyToSelection((start,end,line) -> line.markSelectedForeground(start, end, fg));
    }

    public void applyToSelection(FormattingFunction f) {
        int normalisedStartx, normalisedStarty, normalisedEndx, normalisedEndy;
        if (isForwardsSelection()) {
            normalisedStartx = startx;
            normalisedStarty = starty;
            normalisedEndx = endx;
            normalisedEndy = endy;
        } else {
            normalisedStartx = endx;
            normalisedStarty = endy;
            normalisedEndx = startx;
            normalisedEndy = starty;
        }

        if (normalisedStarty == normalisedEndy) {
            LogFileDisplayRow line = getLine(normalisedStarty);
            f.apply(normalisedStartx, normalisedEndx, line);
        } else {
            for (int yy = normalisedStarty; yy <= normalisedEndy; yy++) {
                LogFileDisplayRow line = getLine(yy);
                if (yy == normalisedStarty) {
                    // First Line
                    f.apply(normalisedStartx, line.getText().length(), line);
                } else if (yy == normalisedEndy) {
                    // Last Line
                    int end = normalisedEndx;
                    if (end < 0) end = line.getText().length();
                    f.apply(0, end, line);
                } else {
                    // Middle line
                    f.apply(0, line.getText().length(), line);
                }
            }
        }
    }

}
