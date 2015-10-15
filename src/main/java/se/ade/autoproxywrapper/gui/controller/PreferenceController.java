package se.ade.autoproxywrapper.gui.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import se.ade.autoproxywrapper.model.ForwardProxy;

public class PreferenceController {

    @FXML
    public ListView<ForwardProxy> hostList;

    @FXML
    public void initialize() {
        hostList.setItems(FXCollections.observableArrayList());
    }

}
