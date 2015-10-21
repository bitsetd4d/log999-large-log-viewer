package com.blinglog.poc;

import com.blinglog.poc.control.ScrollableEditor;
import com.blinglog.poc.task.TaskRunner;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;


import java.net.URL;
import java.util.ResourceBundle;


public class EditorController implements Initializable {

    private ScrollableEditor editor = ScrollableEditor.newEditor();

    // Toolbar
    @FXML private Button openButton;
    @FXML private ToggleButton wrapToggleButton;
    @FXML private Button buttonZoomIn;
    @FXML private Button buttonZoomOut;
    @FXML private Button buttonMarkSelected;
    @FXML private Button buttonBold;
    @FXML private Button buttonForegroundHighlight;
    @FXML private Button buttonBackgroundHighlight;
    @FXML private ColorPicker backgroundColourPicker;
    @FXML private ColorPicker foregroundColourPicker;

    @FXML private TabPane mainTabPane;

    @FXML private ProgressIndicator progressSpinner;
    @FXML private Label progressLabel;
    @FXML private ProgressBar progressBar;

    @FXML private SplitMenuButton backgroundColourSplitMenu;
    @FXML private SplitMenuButton foregroundColourSplitMenu;

    @FXML private TextField quickFilterText;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        hookToolbar();
        editor.setup(mainTabPane);
        TaskRunner.getInstance().setControls(progressSpinner,progressLabel,progressBar);
    }

    private void hookToolbar() {
        buttonZoomIn.setOnAction(ev -> editor.getDisplayProperties().increaseFontSize());
        buttonZoomOut.setOnAction(ev -> editor.getDisplayProperties().decreaseFontSize());
        openButton.setOnAction(ev -> editor.openLogFile());
        buttonMarkSelected.setOnAction(ev -> editor.markSelectedRows());
        //wrapCheckbox.setOnAction(ev -> editor.getDisplayProperties().lineWrapToWindowProperty().setValue(wrapCheckbox.isSelected()));
        wrapToggleButton.setOnAction(ev -> editor.getDisplayProperties().lineWrapToWindowProperty().setValue(wrapToggleButton.isSelected()));

        buttonBold.setOnAction(ev -> editor.makeSelectedBold());
        buttonForegroundHighlight.setOnAction(ev -> editor.makeSelectedHighlightForeground(foregroundColourPicker.getValue()));
        buttonBackgroundHighlight.setOnAction(ev -> editor.makeSelectedHighlightBackground(backgroundColourPicker.getValue()));
        backgroundColourPicker.getStyleClass().add("button");
        backgroundColourPicker.setValue(Color.valueOf("#C1FD33"));
        foregroundColourPicker.getStyleClass().add("button");
        foregroundColourPicker.setValue(Color.valueOf("#FF0000"));

        //backgroundColourSplitMenu.setStyle("-fx-background-color: aqua");
        //backgroundColourSplitMenu.setBackground(new Background(new BackgroundFill(Color.CORNFLOWERBLUE, CornerRadii.EMPTY, Insets.EMPTY)));
        backgroundColourSplitMenu.setText("");
        Label l = new Label("B");
        l.setBackground(new Background(new BackgroundFill(Color.CORNFLOWERBLUE, CornerRadii.EMPTY, Insets.EMPTY)));
        backgroundColourSplitMenu.setGraphic(l);

        foregroundColourSplitMenu.setText("");
        Label l2 = new Label("F");
        l2.setBackground(new Background(new BackgroundFill(Color.CORNFLOWERBLUE, CornerRadii.EMPTY, Insets.EMPTY)));
        foregroundColourSplitMenu.setGraphic(l2);


        //Node node = backgroundColourSplitMenu.get
        //System.out.println("Node is... "+node);
        //node.setBackground(new Background(new BackgroundFill(Color.BLUEVIOLET, CornerRadii.EMPTY, Insets.EMPTY)));
        backgroundColourSplitMenu.setOnAction(ev -> System.out.println("ACTION"));
        backgroundColourSplitMenu.setOnContextMenuRequested(ev -> System.out.println("MENU REQ") );

        quickFilterText.textProperty().addListener((obs,old,nv) -> editor.onQuickFilterTextChanged(nv) );

    }

    @FXML public void onQuickFilterButtonPressed(ActionEvent ev) {
        System.out.println("FILTER BUTTON PRESSED");
    }

    public void onMainSceneScrollEvent(ScrollEvent ev) {
        editor.applyScrollEvent(ev);
    }


    public void onOpenFileRequested() {
        editor.openLogFile();
    }
}
