package se.ade.autoproxywrapper.gui.controller;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import se.ade.autoproxywrapper.config.Config;
import se.ade.autoproxywrapper.events.EventBus;
import se.ade.autoproxywrapper.events.GenericLogEvent;
import se.ade.autoproxywrapper.events.RestartEvent;
import se.ade.autoproxywrapper.model.ForwardProxy;

import static se.ade.autoproxywrapper.config.Config.getConfig;

public class HostListController {
	private enum Mode {
		BLOCKED_HOSTS,
		DIRECT_MODE_HOSTS
	}

	private Mode mode;

    @FXML
    public ListView<String> hostList;

    @FXML
    public TextField newHostName;

    @FXML
    public Button addButton;

    @FXML
    public Button saveButton;

    private String selectedHost;
    private ObservableList<String> items = FXCollections.observableArrayList();
    private Stage window;

    @FXML
    public void initialize() {
        hostList.setPlaceholder(new Label("No hosts configured."));
        hostList.getSelectionModel().selectedItemProperty().addListener(getHostChangeListener());
        hostList.setCellFactory(param -> new HostsListCell());
        Bindings.bindContent(hostList.getItems(), items);
    }

    private ChangeListener<String> getHostChangeListener() {
        return (observable, oldValue, newValue) -> select(newValue);
    }

    @FXML
    public void saveHost(Event event) {
        if (event instanceof KeyEvent && ((KeyEvent) event).getCode() != KeyCode.ENTER) {
            return;
        }
        if (!validateInput()) {
            return;
        }
        if (selectedHost != null) {
			String newText = newHostName.getText();
			items.add(items.indexOf(selectedHost), newText);
			items.remove(selectedHost);
            selectedHost = newText;
        } else {
            items.add(newHostName.getText());
            newHostName.requestFocus();
        }
        deselect();
        refresh();
    }

    @FXML
    public void cancel() {
        window.close();
    }

    @FXML
    public void save() {
		if(mode == Mode.BLOCKED_HOSTS) {
			getConfig().setBlockedHosts(items);
		} else if(mode == Mode.DIRECT_MODE_HOSTS) {
			getConfig().setDirectModeHosts(items);
		}

        Config.save();
        window.close();
    }

	public void editBlockedHosts() {
		items.addAll(getConfig().getBlockedHosts());
		mode = Mode.BLOCKED_HOSTS;
	}

	public void editDirectModeHosts() {
		items.addAll(getConfig().getDirectModeHosts());
		mode = Mode.DIRECT_MODE_HOSTS;
	}

    private boolean validateInput() {
        if (newHostName.getText().equals("")) {
            if (!newHostName.getStyleClass().contains("invalid-field")) {
                newHostName.getStyleClass().add("invalid-field");
            }
            return false;
        }
        newHostName.getStyleClass().remove("invalid-field");
        return true;
    }

    private void resetValidation() {
        newHostName.getStyleClass().remove("invalid-field");
    }

    private void select(String host) {
        selectedHost = host;
        newHostName.setText(selectedHost);
        addButton.setVisible(false);
        saveButton.setVisible(true);
        resetValidation();
    }

    private void deselect() {
        selectedHost = null;
        newHostName.setText("");
        hostList.getSelectionModel().select(null);
        saveButton.setVisible(false);
        addButton.setVisible(true);
        resetValidation();
    }

    private void refresh() {
        hostList.getProperties().put("listRecreateKey", Boolean.TRUE);
    }

    public HostListController setWindow(Stage window) {
        this.window = window;
		return this;
    }

    public class HostsListCell extends ListCell<String> {
        @Override
        protected void updateItem(String item, boolean empty) {
            if (empty) {
                setText("");
                setOnMouseClicked(event -> deselect());
            } else {
                ContextMenu contextMenu = new ContextMenu();
                MenuItem removeMenuItem = new MenuItem("Remove", new ImageView(getClass().getResource("/icon/Minus.png").toExternalForm()));
                removeMenuItem.setOnAction(getRemoveEvent());
                contextMenu.getItems().add(removeMenuItem);
                setContextMenu(contextMenu);

                setText(item);
                setOnMouseClicked(null);
            }
            super.updateItem(item, empty);
        }

        private EventHandler<ActionEvent> getRemoveEvent() {
            return event -> {
                for (String host : items) {
                    if (host.equals(hostList.getSelectionModel().getSelectedItem())) {
                        items.remove(host);
                        deselect();
                        break;
                    }
                }
                refresh();
            };
        }
    }
}
