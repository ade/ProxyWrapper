package se.ade.autoproxywrapper.gui.controller;

import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import se.ade.autoproxywrapper.Main;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static javafx.stage.StageStyle.UNDECORATED;

public class AboutController {

    public void initialize() throws Exception {
        try {
            Font.loadFont(Main.class.getResource("/font/Quicksand_Book.otf").toExternalForm(), 32);

            WebView view = new WebView();
            view.setContextMenuEnabled(false);
            WebEngine engine = view.getEngine();
            addJavaToJavascriptBridge(engine);
            engine.setUserStyleSheetLocation(Main.class.getResource("/view/about.css").toExternalForm());
            engine.load(Main.class.getResource("/view/about.html").toExternalForm());

            Stage stage = new Stage(UNDECORATED);
            stage.setResizable(false);
            Scene scene = new Scene(view, 250, 320);
            stage.setScene(scene);
            stage.show();

            stage.focusedProperty().addListener(observable -> {
                stage.close();
            });
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    private void addJavaToJavascriptBridge(WebEngine engine) {
        JSObject jso = (JSObject) engine.executeScript("window");
        jso.setMember("java", new Bridge());
    }

    public class Bridge {
        public void openGithub() {
            try {
                Desktop.getDesktop().browse(new URI("https://github.com/ade2/AutoProxyWrapper"));
            } catch (IOException | URISyntaxException e) {
                System.err.println("What the hell :(");
            }
        }
    }

}
