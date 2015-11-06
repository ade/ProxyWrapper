package se.ade.autoproxywrapper.gui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import se.ade.autoproxywrapper.config.Config;
import se.ade.autoproxywrapper.ProxyMode;
import se.ade.autoproxywrapper.events.EventBus;
import se.ade.autoproxywrapper.events.GenericLogEvent;
import se.ade.autoproxywrapper.events.RestartEvent;
import se.ade.autoproxywrapper.events.SetModeEvent;

import static se.ade.autoproxywrapper.config.Config.get;

public class PropertiesController {

    @FXML
    public TextField listeningPort;

    @FXML
    public CheckBox enabled;

    @FXML
    public CheckBox verboseLogging;

	@FXML
	public CheckBox minimizeOnStartup;

    private Stage propertiesWindow;

    @FXML
    public void initialize() {
        listeningPort.setText(Integer.toString(get().getLocalPort()));
        enabled.setSelected(get().isEnabled());
		minimizeOnStartup.setSelected(get().isStartMinimized());
        verboseLogging.setSelected(get().isVerboseLogging());
    }

    @FXML
    public void save() {
        if (!validateInput()) {
            return;
        }
        int newLocalPort = Integer.parseInt(listeningPort.getText());
        if(get().getLocalPort() != newLocalPort) {
            get().setLocalPort(newLocalPort);
            EventBus.get().post(GenericLogEvent.info("Restarting..."));
            EventBus.get().post(new RestartEvent());
        }
		get().setStartMinimized(minimizeOnStartup.isSelected());
        get().setVerboseLogging(verboseLogging.isSelected());
        Config.save();

        if(enabled.isSelected() != get().isEnabled()) {
            EventBus.get().post(new SetModeEvent(enabled.isSelected() ? ProxyMode.AUTO : ProxyMode.DISABLED));
        }

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
