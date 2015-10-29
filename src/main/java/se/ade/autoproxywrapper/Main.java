package se.ade.autoproxywrapper;

import com.google.common.eventbus.Subscribe;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import se.ade.autoproxywrapper.events.*;
import se.ade.autoproxywrapper.gui.SystemTrayIcon;
import se.ade.autoproxywrapper.gui.controller.MenuController;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main extends Application {

    private ExecutorService pool = Executors.newSingleThreadExecutor();

    private Stage primaryStage;

    private MiniHttpProxy proxy;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            Platform.setImplicitExit(false);
            EventBus.get().register(this);
            this.primaryStage = primaryStage;
            primaryStage.getIcons().add(new Image("/icon/icon512.png"));
            primaryStage.setTitle("Mini Proxy");
            loadMain();
            loadLogView();
			if(!Config.config().isStartMinimized()) {
            	primaryStage.show();
			}

            EventBus.get().post(GenericLogEvent.info("Starting Mini Proxy..."));

            proxy = new MiniHttpProxy();
            pool.submit(proxy);
            new SystemTrayIcon(this);
        } catch (Exception e) {
            e.printStackTrace();
            closeApplication();
        }
    }

    private void loadMain() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getClassLoader().getResource("view/main.fxml"));
        BorderPane pane = loader.load();

        Scene scene = new Scene(pane);
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(event -> primaryStage.hide());
        primaryStage.setOnHiding(event -> primaryStage.hide());
		primaryStage.setOnShown(event -> EventBus.get().post(new ApplicationShowedEvent()));

        loader.<MenuController>getController().setMain(this);
    }

    private void loadLogView() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getClassLoader().getResource("view/log.fxml"));

        BorderPane pane = (BorderPane) primaryStage.getScene().getRoot();
        pane.setCenter(loader.load());
    }

    public void closeApplication() {
        Platform.exit();
        primaryStage.close();
        pool.shutdownNow();
        try {
            pool.awaitTermination(3, TimeUnit.SECONDS);
            System.exit(0);
        } catch (InterruptedException e) {
            System.exit(1);
        }
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    @Subscribe
    public void setModeEvent(SetModeEvent event) {
        if(event.mode == ProxyMode.DISABLED) {
            Config.config().setEnabled(false);

        } else if(event.mode == ProxyMode.AUTO) {
            Config.config().setEnabled(true);
        }
        Config.save();
        EventBus.get().post(GenericLogEvent.info("Restarting..."));
        EventBus.get().post(new RestartEvent());
    }

    @Subscribe
    public void shutdownEvent(ShutDownEvent event) {
        closeApplication();
    }
}
