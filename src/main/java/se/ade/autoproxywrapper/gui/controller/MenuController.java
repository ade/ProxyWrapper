package se.ade.autoproxywrapper.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;

import static javafx.stage.StageStyle.DECORATED;

public class MenuController {

    @FXML
    public void initialize() {}

    @FXML
    public void menuPreference() throws IOException {
        //TODO Implement in later commits
        /*FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getClassLoader().getResource("view/preferences.fxml"));
        GridPane pane = loader.load();

        Stage stage = new Stage(DECORATED);
        Scene scene = new Scene(pane, 600, 400);
        stage.setScene(scene);
        stage.show();*/
    }

    @FXML
    public void menuClose() {
        //TODO shutdown gracefully
        System.exit(0);
    }

    @FXML
    public void menuAbout() throws Exception {
        AboutController aboutController = new AboutController();
        aboutController.initialize();
    }
}
