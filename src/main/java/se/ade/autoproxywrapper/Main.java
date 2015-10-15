package se.ade.autoproxywrapper;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

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
        this.primaryStage = primaryStage;
        primaryStage.getIcons().add(new Image(Main.class.getResource("/assets/icon512.png").toExternalForm()));
        primaryStage.getIcons().add(new Image(Main.class.getResource("/assets/icon-apple-512.icns").toExternalForm()));
        primaryStage.setTitle("Mini Proxy");
        try {
            loadMain();
            loadLogView();
            attachEvents();
        } catch (IOException e) {
            e.printStackTrace();
        }
        primaryStage.show();

        proxy = new MiniHttpProxy();
        pool.submit(proxy);
    }

    private void loadMain() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getClassLoader().getResource("view/main.fxml"));
        BorderPane pane = loader.load();

        Scene scene = new Scene(pane);
        primaryStage.setScene(scene);
    }

    private void loadLogView() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getClassLoader().getResource("view/log.fxml"));

        BorderPane pane = (BorderPane) primaryStage.getScene().getRoot();
        pane.setCenter(loader.load());
    }

    public void attachEvents() {
        primaryStage.setOnCloseRequest(event -> {
            primaryStage.close();
            pool.shutdownNow();
            try {
                pool.awaitTermination(3, TimeUnit.SECONDS);
                System.exit(0);
            } catch (InterruptedException e) {
                System.exit(1);
            }
        });
    }
}
