package se.ade.autoproxywrapper;

import com.google.common.eventbus.Subscribe;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import se.ade.autoproxywrapper.events.EventBus;
import se.ade.autoproxywrapper.events.ShutDownEvent;
import se.ade.autoproxywrapper.gui.controller.MenuController;
import se.ade.autoproxywrapper.gui.controller.SystemTrayIcon;

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
            EventBus.get().register(this);
            this.primaryStage = primaryStage;
            primaryStage.getIcons().add(new Image(getClass().getResource("/assets/icon512.png").toExternalForm()));
            primaryStage.getIcons().add(new Image(getClass().getResource("/assets/icon-apple-512.icns").toExternalForm()));
            primaryStage.setTitle("Mini Proxy");
            loadMain();
            loadLogView();
            primaryStage.show();

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
        primaryStage.setOnCloseRequest(event -> {
            closeApplication();
        });
        primaryStage.setOnHiding(event -> {
            primaryStage.hide();
        });

        loader.<MenuController>getController().setMain(this);
    }

    private void loadLogView() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getClassLoader().getResource("view/log.fxml"));

        BorderPane pane = (BorderPane) primaryStage.getScene().getRoot();
        pane.setCenter(loader.load());
    }

    @Subscribe
    public void shutdownEvent(ShutDownEvent event) {
        closeApplication();
    }

    public void closeApplication() {
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
}
