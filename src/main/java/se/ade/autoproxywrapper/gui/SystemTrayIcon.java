package se.ade.autoproxywrapper.gui;

import static se.ade.autoproxywrapper.config.Config.get;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.google.common.eventbus.Subscribe;
import javafx.application.Platform;
import se.ade.autoproxywrapper.Labels;
import se.ade.autoproxywrapper.Main;
import se.ade.autoproxywrapper.ProxyMode;
import se.ade.autoproxywrapper.events.*;

public class SystemTrayIcon {

    private TrayIcon trayIcon;
    private MenuItem currentStateItem;
    private MenuItem toggleItem;
    private Main application;
	private Labels labels = Labels.get();

    public SystemTrayIcon(Main main) throws IOException, AWTException {
        this.application = main;
        if (SystemTray.isSupported()) {
            EventBus.get().register(this);

            SystemTray tray = SystemTray.getSystemTray();

            PopupMenu popupMenu = new PopupMenu();
            MenuItem showItem = new MenuItem(labels.get("actions.open-app"));
            showItem.addActionListener(getOpenEventHandler());

            MenuItem closeItem = new MenuItem(labels.get("actions.exit"));
            closeItem.addActionListener(getCloseEventHandler());

            currentStateItem = new MenuItem(getStateText(get().isEnabled()));
            currentStateItem.setEnabled(false);

            toggleItem = new MenuItem(getToggleText(get().isEnabled()));
            toggleItem.addActionListener(getEnableEventHandler());

            popupMenu.add(showItem);
            popupMenu.addSeparator();
            popupMenu.add(currentStateItem);
            popupMenu.add(toggleItem);
            popupMenu.addSeparator();
            popupMenu.add(closeItem);
            BufferedImage image = ImageIO.read(getClass().getResource(getIconFileName()));
			Dimension trayIconSize = tray.getTrayIconSize();
			trayIcon = new TrayIcon(image.getScaledInstance(trayIconSize.width, trayIconSize.height, Image.SCALE_SMOOTH), labels.get("app.name"));
            trayIcon.addActionListener(getOpenEventHandler());
            trayIcon.setPopupMenu(popupMenu);
            tray.add(trayIcon);
        }
    }

	private String getIconFileName() {
		return System.getProperty("os.name").contains("Linux") ? "/icon/iconflat512.png" : "/icon/icon512.png";
	}

	private ActionListener getOpenEventHandler() {
        return event -> Platform.runLater(() -> {
            application.getPrimaryStage().show();
            application.getPrimaryStage().toFront();
            application.getPrimaryStage().requestFocus();
        });
    }

    private ActionListener getEnableEventHandler() {
        return event -> Platform.runLater(() -> {
            if(toggleItem.getLabel().equals(labels.get("mode.change-to-auto"))) {
                EventBus.get().post(new SetModeEvent(ProxyMode.AUTO));
            } else {
                EventBus.get().post(new SetModeEvent(ProxyMode.DISABLED));
            }
        });
    }

    private ActionListener getCloseEventHandler() {
        return event -> Platform.runLater(() -> {
            EventBus.get().post(new ShutDownEvent());
        });
    }

    private String getStateText(boolean enabled) {
        return labels.get("generic.proxy-mode") + ": " + (enabled ? labels.get("mode.auto") : labels.get("mode.direct"));
    }

    private String getToggleText(boolean enabled) {
        return enabled ? labels.get("mode.change-to-direct") : labels.get("mode.change-to-auto");
    }

    @Subscribe
    public void setModeEvent(SetModeEvent event) {
        currentStateItem.setLabel(getStateText(event.mode == ProxyMode.AUTO));
        toggleItem.setLabel(getToggleText(event.mode == ProxyMode.AUTO));
    }
}
