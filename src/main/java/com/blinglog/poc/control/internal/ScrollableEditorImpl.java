package com.blinglog.poc.control.internal;

import com.blinglog.poc.control.ScrollableEditor;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;

/**
 *
 */
public class ScrollableEditorImpl implements ScrollableEditor {

    private DisplayProperties defaultDisplayProperties = new DisplayProperties();

    private TabPane tabPane;

    @Override
    public DisplayProperties getDisplayProperties() {
        ScrollableEditorTab selected = getCurrentTab();
        if (selected != null) {
            return selected.getDisplayProperties();
        }
        return defaultDisplayProperties;
    }

    private ScrollableEditorTab getCurrentTab() {
        if (tabPane != null) {
            ScrollableEditorTab tab = (ScrollableEditorTab) tabPane.getSelectionModel().getSelectedItem();
            return tab;
        }
        return null;
    }

    @Override
    public void setup(TabPane tabPane) {
        this.tabPane = tabPane;
        newTab();
    }

    @Override
    public void openLogFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Log File");
        File selectedFile = fileChooser.showOpenDialog(tabPane.getScene().getWindow());
        if (selectedFile != null) {
            ScrollableEditorTab tab = newTab();
            tab.readFile(selectedFile);
        }
    }

    @Override
    public void markSelectedRows() {
        ScrollableEditorTab selected = getCurrentTab();
        if (selected != null) {
            selected.markSelectedRows();
        }
    }

    @Override
    public void applyScrollEvent(ScrollEvent ev) {
        ScrollableEditorTab selected = getCurrentTab();
        if (selected != null) {
            selected.applyScrollEvent(ev);
        }
    }

    @Override
    public void makeSelectedBold() {
        ScrollableEditorTab selected = getCurrentTab();
        if (selected != null) {
            selected.makeSelectedBold();
        }
    }

    @Override
    public void makeSelectedHighlightForeground(Color value) {
        ScrollableEditorTab selected = getCurrentTab();
        if (selected != null) {
            selected.makeSelectedHighlightForeground(value);
        }
    }

    @Override
    public void makeSelectedHighlightBackground(Color value) {
        ScrollableEditorTab selected = getCurrentTab();
        if (selected != null) {
            selected.makeSelectedHighlightBackground(value);
        }
    }

    @Override
    public void onQuickFilterTextChanged(String filterText) {
        //xx
    }

    private ScrollableEditorTab newTab() {
        ScrollableEditorTab tab = new ScrollableEditorTab();
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
        return tab;
    }


}
