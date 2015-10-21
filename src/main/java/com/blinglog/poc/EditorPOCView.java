package com.blinglog.poc;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;


public class EditorPOCView extends Application {

    private static double WIDTH = 400;
    private static double HEIGHT = 800;

    private Scene mainScene;
    private EditorController controller;

    public static void main(String[] args) {
//        System.setProperty("prism.lcdtext", "true");
//        System.setProperty("prism.text", "t2k");
        Application.launch(EditorPOCView.class, args);

    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("LOG999");
        mainScene = new Scene(createContent(), WIDTH, HEIGHT, Color.web("#000000"));
        primaryStage.setScene(mainScene);
        mainScene.setOnScroll(ev -> controller.onMainSceneScrollEvent(ev));
        primaryStage.show();
    }

    public Parent createContent() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(EditorPOCView.class.getResource("/editor.fxml"));
        AnchorPane ap = loader.load();
        controller = loader.getController();
        System.out.println(controller);
        MainMenuBuilder menu = new MainMenuBuilder(controller);
        ap.getChildren().add(menu.createMainMenuBar());
        return ap;
    }

}
