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
import se.ade.autoproxywrapper.events.SetEnabledEvent;

import static se.ade.autoproxywrapper.config.Config.getConfig;

public class PropertiesController {

    @FXML
    public TextField listeningPort;

    @FXML
    public CheckBox enabled;

    @FXML
    public CheckBox verboseLogging;

	@FXML
	public CheckBox minimizeOnStartup;

	@FXML
	public CheckBox useBlockedHosts;

	@FXML
	public CheckBox useDirectModeHosts;

    private Stage propertiesWindow;

    @FXML
    public void initialize() {
        listeningPort.setText(Integer.toString(getConfig().getLocalPort()));
        enabled.setSelected(getConfig().isEnabled());
		minimizeOnStartup.setSelected(getConfig().isStartMinimized());
        verboseLogging.setSelected(getConfig().isVerboseLogging());
		useBlockedHosts.setSelected(getConfig().isBlockedHostsEnabled());
		useDirectModeHosts.setSelected(getConfig().isDirectModeHostsEnabled());
    }

    @FXML
    public void save() {
        if (!validateInput()) {
            return;
        }
        int newLocalPort = Integer.parseInt(listeningPort.getText());
        if(getConfig().getLocalPort() != newLocalPort) {
            getConfig().setLocalPort(newLocalPort);
            EventBus.get().post(GenericLogEvent.info("Restarting..."));
            EventBus.get().post(new RestartEvent());
        }
		getConfig().setStartMinimized(minimizeOnStartup.isSelected());
        getConfig().setVerboseLogging(verboseLogging.isSelected());
		getConfig().setBlockedHostsEnabled(useBlockedHosts.isSelected());
		getConfig().setDirectModeHostsEnabled(useDirectModeHosts.isSelected());
        Config.save();

        if(enabled.isSelected() != getConfig().isEnabled()) {
            EventBus.get().post(new SetEnabledEvent(enabled.isSelected()));
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
