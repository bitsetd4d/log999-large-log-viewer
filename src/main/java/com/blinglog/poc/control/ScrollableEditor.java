package com.blinglog.poc.control;

import com.blinglog.poc.control.internal.DisplayProperties;
import com.blinglog.poc.control.internal.ScrollableEditorImpl;
import com.blinglog.poc.file.LogFileAccess;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TabPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

/**
 *
 */
public interface ScrollableEditor {

    static ScrollableEditor newEditor() {
        return new ScrollableEditorImpl();
    }

    DisplayProperties getDisplayProperties();
    void setup(TabPane mainTabPane);

    void openLogFile();
    void markSelectedRows();

    void applyScrollEvent(ScrollEvent ev);

    void makeSelectedBold();
    void makeSelectedHighlightForeground(Color value);
    void makeSelectedHighlightBackground(Color value);

    void onQuickFilterTextChanged(String filterText);
}
