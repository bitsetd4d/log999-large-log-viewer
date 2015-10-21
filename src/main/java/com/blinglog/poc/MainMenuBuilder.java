package com.blinglog.poc;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainMenuBuilder {

    private static Logger logger = LoggerFactory.getLogger(MainMenuBuilder.class);

    private MenuBar menuBar = new MenuBar();
    private EditorController controller;

    public MainMenuBuilder(EditorController controller) {
        this.controller = controller;
    }

    public MenuBar createMainMenuBar() {
        createFileMenu();
        createEditMenu();
        createSelectedMenu();
        createSearchMenu();
        createViewMenu();
        createToolsMenu();
        createHelpMenu();
        menuBar.setUseSystemMenuBar(true);
        return menuBar;
    }

    private void createFileMenu() {
        Menu fileMenu = new Menu("File");
        MenuItem openFile = new MenuItem("Open File...");
        MenuItem openRemote = new MenuItem("Open Remote...");
        fileMenu.getItems().addAll(openFile,openRemote);
        menuBar.getMenus().add(fileMenu);
        openFile.setOnAction(ev -> controller.onOpenFileRequested());
    }

    private void createEditMenu() {
        Menu editMenu = new Menu("Edit");
        MenuItem menu1 = new MenuItem("Option 1...");
        MenuItem menu2 = new MenuItem("Option 1...");
        editMenu.getItems().addAll(menu1,menu2);
        menuBar.getMenus().add(editMenu);
    }

    private void createSelectedMenu() {
        Menu menu = new Menu("Selected");
        MenuItem menu1 = new MenuItem("Option 1...");
        MenuItem menu2 = new MenuItem("Option 1...");
        menu.getItems().addAll(menu1,menu2);
        menuBar.getMenus().add(menu);
    }

    private void createSearchMenu() {
        Menu menu = new Menu("Search");
        MenuItem menu1 = new MenuItem("Option 1...");
        MenuItem menu2 = new MenuItem("Option 1...");
        menu.getItems().addAll(menu1,menu2);
        menuBar.getMenus().add(menu);
    }

    private void createViewMenu() {
        Menu menu = new Menu("View");
        MenuItem menu1 = new MenuItem("Option 1...");
        MenuItem menu2 = new MenuItem("Option 1...");
        menu.getItems().addAll(menu1,menu2);
        menuBar.getMenus().add(menu);
    }

    private void createToolsMenu() {
        Menu menu = new Menu("Tools");
        MenuItem menu1 = new MenuItem("Option 1...");
        MenuItem menu2 = new MenuItem("Option 1...");
        menu.getItems().addAll(menu1,menu2);
        menuBar.getMenus().add(menu);
    }

    private void createHelpMenu() {
        Menu menu = new Menu("Help");
        MenuItem menu1 = new MenuItem("Option 1...");
        MenuItem menu2 = new MenuItem("Option 1...");
        menu.getItems().addAll(menu1,menu2);
        menuBar.getMenus().add(menu);
    }

}
