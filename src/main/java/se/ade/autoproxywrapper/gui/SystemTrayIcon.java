package se.ade.autoproxywrapper.gui;

import static se.ade.autoproxywrapper.config.Config.getConfig;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.google.common.eventbus.Subscribe;
import javafx.application.Platform;
import se.ade.autoproxywrapper.Labels;
import se.ade.autoproxywrapper.Main;
import se.ade.autoproxywrapper.MiniHttpProxy;
import se.ade.autoproxywrapper.ProxyMode;
import se.ade.autoproxywrapper.events.*;

public class SystemTrayIcon {

    private TrayIcon trayIcon;
    private MenuItem currentStateItem;
    private MenuItem toggleItem;
    private Main application;
	private Labels labels = Labels.get();
	private MiniHttpProxy proxy;

    public SystemTrayIcon(Main main, MiniHttpProxy proxy) throws IOException, AWTException {
        this.application = main;
		this.proxy = proxy;
        if (SystemTray.isSupported()) {
            EventBus.get().register(this);

            SystemTray tray = SystemTray.getSystemTray();

            PopupMenu popupMenu = new PopupMenu();
            MenuItem showItem = new MenuItem(labels.get("actions.open-app"));
            showItem.addActionListener(getOpenEventHandler());

            MenuItem closeItem = new MenuItem(labels.get("actions.exit"));
            closeItem.addActionListener(getCloseEventHandler());

            currentStateItem = new MenuItem(getStateText(getConfig().isEnabled()));
            currentStateItem.setEnabled(false);

            toggleItem = new MenuItem(getToggleText(getConfig().isEnabled()));
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
                EventBus.get().post(new SetEnabledEvent(true));
				toggleItem.setLabel(labels.get("mode.change-to-direct"));
            } else {
                EventBus.get().post(new SetEnabledEvent(false));
				toggleItem.setLabel(labels.get("mode.change-to-auto"));
            }
        });
    }

    private ActionListener getCloseEventHandler() {
        return event -> Platform.runLater(() -> {
            EventBus.get().post(new ShutDownEvent());
        });
    }

    private String getStateText(boolean enabled) {
        String mode = labels.get("generic.proxy-mode") + ": " + (enabled ? labels.get("mode.auto") : labels.get("mode.direct"));
		if(enabled && proxy.getMode() != null) {
			mode += " (" + proxy.getMode().getName() + ")";
		}
		return mode;
    }

    private String getToggleText(boolean enabled) {
        return enabled ? labels.get("mode.change-to-direct") : labels.get("mode.change-to-auto");
    }
}
