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
import se.ade.autoproxywrapper.loopback.LoopBackConfig;
import se.ade.autoproxywrapper.model.ForwardProxy;

import static se.ade.autoproxywrapper.config.Config.getConfig;

public class LoopbackController {

	@FXML
	private ListView<LoopBackConfig> hostList;

	@FXML
	private TextField newHostName;

	@FXML
	private TextField newPortLocal;

	@FXML
	private Button addButton;

	@FXML
	private Button updateButton;

	@FXML
	private TextField newPortTarget;

	private LoopBackConfig selectedConfig;
	private ObservableList<LoopBackConfig> items = FXCollections.observableArrayList();
	private Stage window;

	@FXML
	public void initialize() {
		hostList.setPlaceholder(new Label("No loopbacks configured."));
		hostList.getSelectionModel().selectedItemProperty().addListener(getChangeListener());
		hostList.setCellFactory(param -> new LoopbackListCell());
		Bindings.bindContent(hostList.getItems(), items);

		items.addAll(getConfig().getLoopBackConfigs());
	}

	private ChangeListener<LoopBackConfig> getChangeListener() {
		return (observable, oldValue, newValue) -> select(newValue);
	}

	public void setWindow(Stage window) {
		this.window = window;
	}

	@FXML
	void onItemUpdated(Event event) {
		if (event instanceof KeyEvent && ((KeyEvent) event).getCode() == KeyCode.ENTER) {
			onSaveOrAdd();
		}
	}

	@FXML
	void onUpdateItem(Event event) {
		onSaveOrAdd();
	}

	@FXML
	void onAdd(Event event) {
		onSaveOrAdd();
	}

	void onSaveOrAdd() {
		if (!validateInput()) {
			return;
		}
		if (selectedConfig != null) {
			selectedConfig.setRemoteHost(newHostName.getText());
			selectedConfig.setRemotePort(Integer.parseInt(newPortTarget.getText()));
			selectedConfig.setLocalPort(Integer.parseInt(newPortLocal.getText()));
		} else {
			LoopBackConfig newItem = new LoopBackConfig(newHostName.getText(), Integer.parseInt(newPortTarget.getText()), Integer.parseInt(newPortLocal.getText()), "alias");
			items.add(newItem);
			newHostName.requestFocus();
		}
		deselect();
		refresh();
	}

	@FXML
	void cancel(Event event) {
		window.close();
	}

	@FXML
	void onSave(Event event) {
		getConfig().setLoopbackConfigs(items);
		Config.save();
		window.close();
		EventBus.get().post(GenericLogEvent.info("Restarting..."));
		EventBus.get().post(new RestartEvent());
	}

	private boolean validateInput() {
		if (newHostName.getText().equals("")) {
			if (!newHostName.getStyleClass().contains("invalid-field")) {
				newHostName.getStyleClass().add("invalid-field");
			}
			return false;
		}
		newHostName.getStyleClass().remove("invalid-field");
		if (newPortLocal.getText().equals("") || !StringUtils.isNumeric(newPortLocal.getText()) || Integer.parseInt(newPortLocal.getText()) > Short.MAX_VALUE) {
			if (!newPortLocal.getStyleClass().contains("invalid-field")) {
				newPortLocal.getStyleClass().add("invalid-field");
			}
			return false;
		}
		newPortLocal.getStyleClass().remove("invalid-field");

		if (newPortTarget.getText().equals("") || !StringUtils.isNumeric(newPortTarget.getText()) || Integer.parseInt(newPortTarget.getText()) > Short.MAX_VALUE) {
			if (!newPortTarget.getStyleClass().contains("invalid-field")) {
				newPortTarget.getStyleClass().add("invalid-field");
			}
			return false;
		}
		newPortTarget.getStyleClass().remove("invalid-field");

		return true;
	}

	private void select(LoopBackConfig config) {
		selectedConfig = config;
		newHostName.setText(config.getRemoteHost());
		newPortTarget.setText(Integer.toString(config.getRemotePort()));
		newPortLocal.setText(Integer.toString(config.getLocalPort()));
		addButton.setVisible(false);
		updateButton.setVisible(true);
		resetValidation();
	}

	private void deselect() {
		selectedConfig = null;
		newHostName.setText("");
		newPortTarget.setText("");
		newPortLocal.setText("");
		hostList.getSelectionModel().select(null);
		updateButton.setVisible(false);
		addButton.setVisible(true);
		resetValidation();
	}

	private void resetValidation() {
		newHostName.getStyleClass().remove("invalid-field");
		newPortLocal.getStyleClass().remove("invalid-field");
		newPortTarget.getStyleClass().remove("invalid-field");
	}

	private void refresh() {
		hostList.getProperties().put("listRecreateKey", Boolean.TRUE);
	}

	public class LoopbackListCell extends ListCell<LoopBackConfig> {

		@Override
		protected void updateItem(LoopBackConfig item, boolean empty) {
			if (empty) {
				setText("");
				setOnMouseClicked(event -> deselect());
			} else {
				ContextMenu contextMenu = new ContextMenu();
				MenuItem removeMenuItem = new MenuItem("Remove", new ImageView(getClass().getResource("/icon/Minus.png").toExternalForm()));
				removeMenuItem.setOnAction(getRemoveEvent());
				contextMenu.getItems().add(removeMenuItem);
				setContextMenu(contextMenu);

				setText(item.getLocalPort() + " -> " + item.getRemoteHost() + ":" + item.getRemotePort());
				setOnMouseClicked(null);
			}
			super.updateItem(item, empty);
		}

		private EventHandler<ActionEvent> getRemoveEvent() {
			return event -> {
				for (LoopBackConfig config : items) {
					if (config.equals(hostList.getSelectionModel().getSelectedItem())) {
						items.remove(config);
						deselect();
						break;
					}
				}
				refresh();
			};
		}
	}

}
