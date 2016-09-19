package com.blinglog.poc.control.internal;

import com.blinglog.poc.Globals;
import com.blinglog.poc.file.LogFileAccess;
import javafx.geometry.Orientation;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.Tab;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class ScrollableEditorTab extends Tab {

    private static Logger logger = LoggerFactory.getLogger(ScrollableEditorTab.class);

    private static double LINE_NUMBER_WIDTH = 65;
    private static double SCROLLBAR_WIDTH = 17;

    private AnchorPane tabRoot = new AnchorPane();

    private DisplayProperties displayProperties = new DisplayProperties();
    private LogFileAccess logFileAccess;
    private LineNumberCanvas lineNumberCanvas = new LineNumberCanvas();
    private ScrollBar verticalScrollbar = new ScrollBar();
    private ScrollBar horizontalScrollbar = new ScrollBar();

    private AnchorPane editorCanvas = new AnchorPane();
    private TextSelectionCanvas textSelectionCanvas = new TextSelectionCanvas();
    private LogTextCanvas textDisplayCanvas = new LogTextCanvas();

    public ScrollableEditorTab() {
        super();
        setText("Tab");
        logFileAccess = LogFileAccess.newDiskBackedInstance();
        logFileAccess.lineWrapWidthProperty().bind(displayProperties.lineWrapWidthProperty());
        textDisplayCanvas.setMouseTransparent(true);
        textDisplayCanvas.set(logFileAccess, displayProperties);
        textSelectionCanvas.set(logFileAccess, displayProperties);
        verticalScrollbar.setOrientation(Orientation.VERTICAL);
        lineNumberCanvas.set(logFileAccess, displayProperties);
        setupTabRoot();
        setContent(tabRoot);
        hookProperties();
    }

    public void readFile(File file) {
        try {
            setText(file.getName());
            logFileAccess.readFile(file.getAbsolutePath());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupTabRoot() {
        /* Line Number Canvas */
        lineNumberCanvas.setWidth(LINE_NUMBER_WIDTH);
        lineNumberCanvas.heightProperty().bind(editorCanvas.heightProperty());
        AnchorPane.setTopAnchor(lineNumberCanvas, 0d);
        AnchorPane.setBottomAnchor(lineNumberCanvas, 0d);
        AnchorPane.setLeftAnchor(lineNumberCanvas, 0d);

        /* Scroll Bars */
        AnchorPane.setTopAnchor(verticalScrollbar, 0d);
        AnchorPane.setBottomAnchor(verticalScrollbar, SCROLLBAR_WIDTH);
        AnchorPane.setRightAnchor(verticalScrollbar, 0d);
        verticalScrollbar.setPrefWidth(SCROLLBAR_WIDTH);

        AnchorPane.setLeftAnchor(horizontalScrollbar, 0d);
        AnchorPane.setRightAnchor(horizontalScrollbar, SCROLLBAR_WIDTH);
        AnchorPane.setBottomAnchor(horizontalScrollbar, 0d);
        horizontalScrollbar.setPrefHeight(SCROLLBAR_WIDTH);

        /* Editor canvases */
        layoutEditorCanvas();
        addToEditorCanvas(textSelectionCanvas);
        addToEditorCanvas(textDisplayCanvas);
        tabRoot.setMinSize(20,20);
        tabRoot.getChildren().addAll(lineNumberCanvas,editorCanvas,verticalScrollbar,horizontalScrollbar);
    }

    private void layoutEditorCanvas() {
        AnchorPane.setTopAnchor(editorCanvas,0d);
        AnchorPane.setLeftAnchor(editorCanvas, LINE_NUMBER_WIDTH);
        AnchorPane.setBottomAnchor(editorCanvas, SCROLLBAR_WIDTH);
        AnchorPane.setRightAnchor(editorCanvas, SCROLLBAR_WIDTH);
    }

    private void addToEditorCanvas(Canvas canvas) {
        editorCanvas.getChildren().add(canvas);
        canvas.widthProperty().bind(editorCanvas.widthProperty());
        canvas.heightProperty().bind(editorCanvas.heightProperty());
        AnchorPane.setTopAnchor(canvas,0d);
        AnchorPane.setLeftAnchor(canvas, 0d);
        AnchorPane.setBottomAnchor(canvas, 0d);
        AnchorPane.setRightAnchor(canvas, 0d);
    }

    private void hookProperties() {
        verticalScrollbar.maxProperty().bind(logFileAccess.numberOfLinesProperty());
        horizontalScrollbar.maxProperty().bind(displayProperties.lineWrapWidthProperty());
        textDisplayCanvas.topIndexProperty().bind(verticalScrollbar.valueProperty());
        textSelectionCanvas.topIndexProperty().bind(verticalScrollbar.valueProperty());
        textDisplayCanvas.leftIndexProperty().bind(horizontalScrollbar.valueProperty());
        textSelectionCanvas.leftIndexProperty().bind(horizontalScrollbar.valueProperty());
        displayProperties.lineWrapToWindowProperty().addListener(ev -> updateForLineWrapToWindow());

        tabRoot.widthProperty().addListener(ev -> updateForLineWrapToWindow());
    }

    private void updateForLineWrapToWindow() {
        logger.info("Updating for window width");
        boolean wrap = displayProperties.lineWrapToWindowProperty().get();
        if (wrap) {
            updateDisplayWrapToWindowWidth();
        } else {
            displayProperties.lineWrapWidthProperty().setValue(Globals.HARD_LINEWRAP);
        }
    }

    private void updateDisplayWrapToWindowWidth() {
        int canSeeWidth = (int) (textDisplayCanvas.getWidth() / displayProperties.charWidthProperty().get());
        displayProperties.lineWrapWidthProperty().setValue(Math.max(Globals.MIN_LINEWRAP,canSeeWidth));
    }

    public DisplayProperties getDisplayProperties() {
        return displayProperties;
    }

    public void markSelectedRows() {
        lineNumberCanvas.markSelectedRows();
    }

    public void applyScrollEvent(ScrollEvent ev) {
        applyToScrollbar(horizontalScrollbar,ev.getDeltaX());
        applyToScrollbar(verticalScrollbar,ev.getDeltaY());
    }

    private void applyToScrollbar(ScrollBar scrollbar,double delta) {
        int count = getVelocity(delta);
        if (delta > 4) {
            for (int i=0; i<count; i++) {
                scrollbar.decrement();
            }
        } else if (delta < -4) {
            for (int i=0; i<count; i++) {
                scrollbar.increment();
            }
        }
    }

    private int getVelocity(double delta) {
        return Math.max(1,(int)Math.abs((delta / 5)));
    }

    public void makeSelectedBold() {
        textSelectionCanvas.makeSelectedBold(true);
        textDisplayCanvas.draw();
    }

    public void makeSelectedHighlightForeground(Color value) {
        textSelectionCanvas.makeSelectedForeground(value);
        textDisplayCanvas.draw();
    }

    public void makeSelectedHighlightBackground(Color value) {
        textSelectionCanvas.makeSelectedBackground(value);
        textDisplayCanvas.draw();
    }
}
