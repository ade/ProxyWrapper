package se.ade.autoproxywrapper.gui.controller;

import static javafx.stage.StageStyle.UNDECORATED;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

public class AboutController {

	public AboutController() {
		Font.loadFont(getClass().getResource("/font/Quicksand_Book.otf").toExternalForm(), 32);
	}

	public void show() throws Exception {
        try {
            WebView view = new WebView();
            view.setContextMenuEnabled(false);
            WebEngine engine = view.getEngine();
            addJavaToJavascriptBridge(engine);
            engine.setUserStyleSheetLocation(getClass().getResource("/view/about.css").toExternalForm());
            engine.load(getClass().getResource("/view/about.html").toExternalForm());

            Stage stage = new Stage(UNDECORATED);
            stage.setResizable(false);
            Scene scene = new Scene(view, 250, 320);
            stage.setScene(scene);
            stage.show();
			stage.requestFocus();

            stage.focusedProperty().addListener((observable, oldValue, newValue) -> {
				if (!newValue) {
					stage.close();
				}
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
