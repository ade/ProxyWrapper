package se.ade.autoproxywrapper.gui.controller;

import com.google.common.eventbus.Subscribe;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import se.ade.autoproxywrapper.Config;
import se.ade.autoproxywrapper.events.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import static se.ade.autoproxywrapper.Config.config;

public class LogController {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.MEDIUM);

    @FXML
    public TextFlow textFlow;

    @FXML
    public ScrollPane logScrollPane;

    private ObservableList<Node> observableListNode = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        EventBus.get().register(this);
        Bindings.bindContent(textFlow.getChildren(), observableListNode);
    }

    private synchronized void addText(String text) {
        Platform.runLater(() -> {
            observableListNode.add(new Text(DATE_TIME_FORMATTER.format(LocalDateTime.now()) + " | " + text + "\n"));
            logScrollPane.setVvalue(logScrollPane.getHeight());
        });
    }

    @Subscribe
    public void genericLogEvent(GenericLogEvent e) {
        if(!e.isVerbose() || config().isVerboseLogging())
        addText(e.getMessage());
    }

    @Subscribe
    public void forwardProxyConnectionFailureEvent(ForwardProxyConnectionFailureEvent e) {
        addText(e.error.toString());
    }

    @Subscribe
    public void requestEvent(RequestEvent e) {
        addText(e.method + " " + e.url);
    }

    @Subscribe
    public void detectModeEvent(DetectModeEvent e) {
        addText("In " + e.mode.getName() + " mode (auto)");
    }
}
