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
import java.util.LinkedList;

import static se.ade.autoproxywrapper.config.Config.get;

public class LogController {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.MEDIUM);
    private static final int LIMIT = 3000;

    @FXML
    public TextFlow textFlow;

    @FXML
    public ScrollPane logScrollPane;

    private LinkedList<Node> list = new LinkedList<>();
    private ObservableList<Node> observableListNode = FXCollections.observableList(list);

    @FXML
    public void initialize() {
        EventBus.get().register(this);
        Bindings.bindContent(textFlow.getChildren(), observableListNode);
    }

    private synchronized void addText(String text) {
        Platform.runLater(() -> {
            if(observableListNode.size() >= LIMIT) {
                observableListNode.remove(0);
            }
            observableListNode.add(new Text(DATE_TIME_FORMATTER.format(LocalDateTime.now()) + " | " + text + "\n"));
            logScrollPane.setVvalue(logScrollPane.getHeight());
        });
    }

	@Subscribe
	public void applicationShowedEvent(ApplicationShowedEvent event) {
		logScrollPane.setVvalue(logScrollPane.getHeight());
	}

    @Subscribe
    public void genericLogEvent(GenericLogEvent e) {
        if(!e.isVerbose() || get().isVerboseLogging())
        addText(e.message);
    }

    @Subscribe
    public void forwardProxyConnectionFailureEvent(ForwardProxyConnectionFailureEvent e) {
        if(e.error != null) {
            addText(e.error.toString());
        }
    }

    @Subscribe
    public void requestEvent(RequestEvent e) {
        if(get().isVerboseLogging()) {
            addText(e.method + " " + e.url);
        }
    }

    @Subscribe
    public void detectModeEvent(DetectModeEvent e) {
        if(e.host != null) {
            addText("In " + e.mode.getName() + " mode to host \"" + e.host.getHostName() + ":" + e.host.getPort() + "\" (auto)");
        } else {
            addText("In " + e.mode.getName() + " mode (auto)");
        }
    }
}
