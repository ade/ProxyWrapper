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
import se.ade.autoproxywrapper.events.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class LogController {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.MEDIUM);

    @FXML
    public TextFlow textFlow;

    @FXML
    public ScrollPane logScrollPane;

    private ObservableList<Node> observableListNode = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        EventBus.get().register(new LogEventListener());
        Bindings.bindContent(textFlow.getChildren(), observableListNode);
    }

    private synchronized void addText(String text) {
        Platform.runLater(() -> {
            observableListNode.add(new Text(DATE_TIME_FORMATTER.format(LocalDateTime.now()) + " | " + text + "\n"));
            logScrollPane.setVvalue(logScrollPane.getHeight());
        });
    }

    private class LogEventListener {
        @Subscribe
        public void onEvent(GenericLogEvent e) {
            addText(e.getMessage());
        }

        @Subscribe
        public void onEvent(ForwardProxyConnectionFailureEvent e) {
            addText(e.error.toString());
        }

        @Subscribe
        public void onEvent(RequestEvent e) {
            addText(e.method + " " + e.url);
        }

        @Subscribe
        public void onEvent(DetectModeEvent e) {
            addText("In " + e.mode.getName() + " mode (auto)");
        }
    }
}
