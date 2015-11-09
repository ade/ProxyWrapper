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

import static se.ade.autoproxywrapper.config.Config.get;

public class ProxiesController {

    @FXML
    public ListView<ForwardProxy> hostList;

    @FXML
    public TextField newHostName;

    @FXML
    public TextField newPort;

    @FXML
    public Button addButton;

    @FXML
    public Button saveButton;

    private ForwardProxy selectedProxy;
    private ObservableList<ForwardProxy> items = FXCollections.observableArrayList();
    private Stage window;

    @FXML
    public void initialize() {
        hostList.setPlaceholder(new Label("No proxies configured."));
        hostList.getSelectionModel().selectedItemProperty().addListener(getForwardProxyChangeListener());
        hostList.setCellFactory(param -> new ForwardProxyListCell());
        Bindings.bindContent(hostList.getItems(), items);

        items.addAll(get().getForwardProxies());
    }

    private ChangeListener<ForwardProxy> getForwardProxyChangeListener() {
        return (observable, oldValue, newValue) -> select(newValue);
    }

    @FXML
    public void saveForwardProxy(Event event) {
        if (event instanceof KeyEvent && ((KeyEvent) event).getCode() != KeyCode.ENTER) {
            return;
        }
        if (!validateInput()) {
            return;
        }
        if (selectedProxy != null) {
            selectedProxy.setHost(newHostName.getText());
            selectedProxy.setPort(Integer.parseInt(newPort.getText()));
        } else {
            ForwardProxy newForwardProxy = new ForwardProxy(newHostName.getText(), Integer.parseInt(newPort.getText()));
            items.add(newForwardProxy);
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
        get().setForwardProxies(items);
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
        if (newPort.getText().equals("") || !StringUtils.isNumeric(newPort.getText()) || Integer.parseInt(newPort.getText()) > Short.MAX_VALUE) {
            if (!newPort.getStyleClass().contains("invalid-field")) {
                newPort.getStyleClass().add("invalid-field");
            }
            return false;
        }
        newPort.getStyleClass().remove("invalid-field");
        return true;
    }

    private void resetValidation() {
        newHostName.getStyleClass().remove("invalid-field");
        newPort.getStyleClass().remove("invalid-field");
    }

    private void select(ForwardProxy proxy) {
        selectedProxy = proxy;
        newHostName.setText(selectedProxy.getHost());
        newPort.setText(Integer.toString(selectedProxy.getPort()));
        addButton.setVisible(false);
        saveButton.setVisible(true);
        resetValidation();
    }

    private void deselect() {
        selectedProxy = null;
        newHostName.setText("");
        newPort.setText("");
        hostList.getSelectionModel().select(null);
        saveButton.setVisible(false);
        addButton.setVisible(true);
        resetValidation();
    }

    private void refresh() {
        hostList.getProperties().put("listRecreateKey", Boolean.TRUE);
    }

    public void setWindow(Stage window) {
        this.window = window;
    }

    public class ForwardProxyListCell extends ListCell<ForwardProxy> {

        @Override
        protected void updateItem(ForwardProxy item, boolean empty) {
            if (empty) {
                setText("");
                setOnMouseClicked(event -> deselect());
            } else {
                ContextMenu contextMenu = new ContextMenu();
                MenuItem removeMenuItem = new MenuItem("Remove", new ImageView(getClass().getResource("/icon/Minus.png").toExternalForm()));
                removeMenuItem.setOnAction(getRemoveEvent());
                contextMenu.getItems().add(removeMenuItem);
                setContextMenu(contextMenu);

                setText(item.getHost() + ":" + item.getPort());
                setOnMouseClicked(null);
            }
            super.updateItem(item, empty);
        }

        private EventHandler<ActionEvent> getRemoveEvent() {
            return event -> {
                for (ForwardProxy proxy : items) {
                    if (proxy.equals(hostList.getSelectionModel().getSelectedItem())) {
                        items.remove(proxy);
                        deselect();
                        break;
                    }
                }
                refresh();
            };
        }
    }
}
