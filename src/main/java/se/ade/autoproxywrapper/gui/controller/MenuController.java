package se.ade.autoproxywrapper.gui.controller;

import static javafx.stage.StageStyle.DECORATED;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import se.ade.autoproxywrapper.Main;
import se.ade.autoproxywrapper.events.EventBus;
import se.ade.autoproxywrapper.events.ShutDownEvent;

public class MenuController {

    private Main main;
	private AboutController aboutController;

    @FXML
    public void initialize() {
		aboutController = new AboutController();
    }

    @FXML
    public void menuProperties() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getClassLoader().getResource("view/properties.fxml"));
        Pane pane = loader.load();

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
            Pane pane = loader.load();

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
	public void menuStatistics() throws IOException{
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(getClass().getClassLoader().getResource("view/statistics.fxml"));
		Pane pane = loader.load();

		Stage stage = new Stage(DECORATED);
		stage.initOwner(main.getPrimaryStage());
		stage.setResizable(true);
		stage.setTitle("Statistics");
		Scene scene = new Scene(pane, 700, 400);
		stage.setScene(scene);
		stage.show();

		loader.<StatisticsController>getController().setWindow(stage);
	}

    @FXML
    public void menuClose() {
        EventBus.get().post(new ShutDownEvent());
    }

    @FXML
    public void menuAbout() throws Exception {
        aboutController.show();
    }

    public void setMain(Main main) {
        this.main = main;
    }
}
