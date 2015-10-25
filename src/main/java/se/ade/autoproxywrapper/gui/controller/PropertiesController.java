package se.ade.autoproxywrapper.gui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import se.ade.autoproxywrapper.Config;
import se.ade.autoproxywrapper.events.EventBus;
import se.ade.autoproxywrapper.events.RestartEvent;

import static se.ade.autoproxywrapper.Config.config;

public class PropertiesController {

    @FXML
    public TextField listeningPort;

    @FXML
    public CheckBox verboseLogging;

    private Stage propertiesWindow;

    @FXML
    public void initialize() {
        listeningPort.setText(Integer.toString(config().getLocalPort()));
        verboseLogging.setSelected(config().isVerboseLogging());
    }

    @FXML
    public void save() {
        if (!validateInput()) {
            return;
        }
        int newLocalPort = Integer.parseInt(listeningPort.getText());
        if(config().getLocalPort() != newLocalPort) {
            config().setLocalPort(newLocalPort);
            EventBus.get().post(new RestartEvent());
        }
        config().setVerboseLogging(verboseLogging.isSelected());
        Config.save();

        propertiesWindow.close();
    }

    @FXML
    public void cancel() {
        propertiesWindow.close();
    }

    private boolean validateInput() {
        if (listeningPort.getText().equals("") || !StringUtils.isNumeric(listeningPort.getText()) || Integer.parseInt(listeningPort.getText()) > Short.MAX_VALUE) {
            if (!listeningPort.getStyleClass().contains("invalid-field")) {
                listeningPort.getStyleClass().add("invalid-field");
            }
            return false;
        }
        listeningPort.getStyleClass().remove("invalid-field");
        return true;
    }

    public void setWindow(Stage window) {
        this.propertiesWindow = window;
    }
}
