package com.blinglog.poc.control.internal;

import com.blinglog.poc.file.LogFileAccess;
import com.log999.display.api.LogFileLine;
import com.log999.display.api.LogFilePage;
import com.blinglog.poc.util.AwesomeIcon;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class LineNumberCanvas extends Canvas {

    private static Logger logger = LoggerFactory.getLogger(LineNumberCanvas.class);

    private LogFileAccess logFileAccess;
    private DisplayProperties displayProperties;

    private boolean inSelection;
    private long starty;
    private long endy;

    private List<LogFileLine> selectedRows = new ArrayList<>();

    public LineNumberCanvas() {
        addEventHandler(MouseEvent.MOUSE_PRESSED, this::onMousePressed);
        addEventHandler(MouseEvent.MOUSE_RELEASED, this::onMouseReleased);
        addEventHandler(MouseEvent.MOUSE_DRAGGED, this::onMouseDragged);
        addEventHandler(MouseEvent.MOUSE_CLICKED, this::onMouseClicked);
    }

    private void onMousePressed(MouseEvent ev) {
        LogFileLine line = getLogFileRowFromMouse(ev.getY());
        starty = line.getLineNumber();
        endy = starty;
        inSelection = true;
        logger.info("Mouse pressed - actual row start {}",starty);
        draw();
    }

    private void onMouseReleased(MouseEvent ev) {
        inSelection = false;
        draw();
    }

    private void onMouseDragged(MouseEvent ev) {
        LogFileLine line = getLogFileRowFromMouse(ev.getY());
        endy = line.getLineNumber();
        logger.info("Mouse drag - range is {} - {}",starty,endy);
        draw();
    }

    private void onMouseClicked(MouseEvent ev) {
        if (ev.getClickCount() == 2) {
            LogFileLine line = getLogFileRowFromMouse(ev.getY());
            logger.info("Double click {}",line.getLineNumber());
        }
    }

    private LogFileLine getLogFileRowFromMouse(double y) {
        LogFilePage logFilePage = logFileAccess.logFilePageProperty().get();
        int displayRow = (int) (y / displayProperties.lineHeightProperty().get());
        return logFilePage.getLogFileLineForDisplayRow(displayRow);
    }

    public void set(LogFileAccess logFileAccess, DisplayProperties displayProperties) {
        this.logFileAccess = logFileAccess;
        this.displayProperties = displayProperties;
        logFileAccess.logFilePageProperty().addListener(ev -> draw());
        displayProperties.fontProperty().addListener(ev -> draw());
    }

    private void draw() {
        double width = getWidth();
        double height = getHeight();
        logger.debug("Draw called - w {} h {}",width,height);
        GraphicsContext gc = getGraphicsContext2D();
        gc.setFill(Color.BLUE);
        gc.clearRect(0, 0, width, height);

        LogFilePage logFilePage = logFileAccess.logFilePageProperty().get();
        logger.info("LineNumber: Logfile page {}",logFilePage);
        if (logFilePage == null) return;

        gc.setFont(displayProperties.lineNumberFontProperty().get());
        if (false) {
            drawDisplayLineNumbers(gc, logFilePage);
        } else {
            drawActualLineNumbers(gc, logFilePage);
        }
    }

    private void drawDisplayLineNumbers(GraphicsContext gc, LogFilePage logFilePage) {
        int count = logFilePage.getDisplayRowCount();
        long top = logFilePage.getTopDisplayRow();
        double y = displayProperties.lineHeightProperty().get();
        for (int i=0; i<count; i++) {
            long displayLineNumber = top + i;
            String lineNumber = String.valueOf(displayLineNumber);
            gc.fillText(lineNumber, 0, y);
            y += displayProperties.lineHeightProperty().get();
        }
    }

    private void drawActualLineNumbers(GraphicsContext gc, LogFilePage logFilePage) {
        double x = getWidth() - 5;
        int count = logFilePage.getRowCount();
        double y = (1 - logFilePage.getFirstLineOffset()) * displayProperties.lineHeightProperty().get();
        gc.setTextAlign(TextAlignment.RIGHT);
        for (int i=0; i<count; i++) {
            LogFileLine row = logFilePage.getRow(i);
            long lineNumber = row.getLineNumber();
            if (isBetween(lineNumber,starty,endy)) {
                if (inSelection) {
                    gc.save();
                    gc.setFill(Color.YELLOW);
                    gc.setEffect(new GaussianBlur());
                    gc.fillText(String.valueOf(lineNumber), x, y);
                    gc.restore();
                }
                gc.setFill(Color.RED);
            } else {
                gc.setFill(Color.BLUE);
            }
            gc.fillText(String.valueOf(lineNumber), x, y);
            if (row.isMarked()) {
                gc.save();
                gc.setFont(displayProperties.getSymbolFont());
                gc.setFill(Color.DARKGREEN);
                gc.fillText(AwesomeIcon.ANGLE_DOUBLE_RIGHT.toString(), 20, y);
                gc.restore();
            }
            y += displayProperties.lineHeightProperty().get() * row.getDisplayRowCount();
        }
    }

    private List<LogFileLine> getSelectedLogFileLines() {
        List<LogFileLine> selected = new ArrayList<>();
        LogFilePage logFilePage = logFileAccess.logFilePageProperty().get();
        int count = logFilePage.getRowCount();
        for (int i=0; i<count; i++) {
            LogFileLine row = logFilePage.getRow(i);
            long lineNumber = row.getLineNumber();
            if (isBetween(lineNumber, starty, endy)) {
                selected.add(row);
            }
        }
        return selected;
    }

    private boolean isBetween(long p,long y1,long y2) {
        if (y1 <= y2) {
            return (p >= y1 && p <= y2);
        }
        return (p >= y2 && p <= y1);
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

    public void markSelectedRows() {
        List<LogFileLine> selected = getSelectedLogFileLines();
        if (selected.isEmpty()) return;
        LogFileLine first = selected.get(0);
        boolean desired = !first.isMarked();
        for (LogFileLine line : selected) {
            line.setMarked(desired);
        }
        //starty = endy = 0;
        draw();
    }
}
