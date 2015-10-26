package se.ade.autoproxywrapper.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import se.ade.autoproxywrapper.Main;
import se.ade.autoproxywrapper.events.EventBus;
import se.ade.autoproxywrapper.events.ShutDownEvent;

import java.io.IOException;

import static javafx.stage.StageStyle.DECORATED;

public class MenuController {

    private Main main;

    @FXML
    public void initialize() {
    }

    @FXML
    public void menuProperties() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getClassLoader().getResource("view/properties.fxml"));
        BorderPane pane = loader.load();

        Stage stage = new Stage(DECORATED);
        stage.initOwner(main.getPrimaryStage());
        stage.setResizable(false);
        stage.setTitle("Properties");
        Scene scene = new Scene(pane, 500, 300);
        stage.setScene(scene);
        stage.show();

        loader.<PropertiesController>getController().setWindow(stage);
    }

    @FXML
    public void menuProxies() throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getClassLoader().getResource("view/proxies.fxml"));
            VBox pane = loader.load();

            Stage stage = new Stage(DECORATED);
            stage.initOwner(main.getPrimaryStage());
            stage.setResizable(false);
            stage.setTitle("Proxies");
            Scene scene = new Scene(pane, 500, 300);
            stage.setScene(scene);
            stage.show();

            loader.<ProxiesController>getController().setWindow(stage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void menuClose() {
        EventBus.get().post(new ShutDownEvent());
    }

    @FXML
    public void menuAbout() throws Exception {
        AboutController aboutController = new AboutController();
        aboutController.initialize();
    }

    public void setMain(Main main) {
        this.main = main;
    }
}
