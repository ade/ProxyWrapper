package se.ade.autoproxywrapper.gui;

import com.google.common.eventbus.Subscribe;
import javafx.application.Platform;
import se.ade.autoproxywrapper.Main;
import se.ade.autoproxywrapper.ProxyMode;
import se.ade.autoproxywrapper.events.EventBus;
import se.ade.autoproxywrapper.events.SetModeEvent;
import se.ade.autoproxywrapper.events.ShutDownEvent;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

import static se.ade.autoproxywrapper.Config.config;

public class SystemTrayIcon {

    private TrayIcon trayIcon;
    private MenuItem currentStateItem;
    private MenuItem toggleItem;
    private Main application;

    public SystemTrayIcon(Main main) throws IOException, AWTException {
        this.application = main;
        if (SystemTray.isSupported()) {
            EventBus.get().register(this);

            SystemTray tray = SystemTray.getSystemTray();

            PopupMenu popupMenu = new PopupMenu();
            MenuItem showItem = new MenuItem("Open Mini Proxy");
            showItem.addActionListener(getOpenEventHandler());

            MenuItem closeItem = new MenuItem("Close");
            closeItem.addActionListener(getCloseEventHandler());

            currentStateItem = new MenuItem(getStateText(config().isEnabled()));
            currentStateItem.setEnabled(false);

            toggleItem = new MenuItem(getToggleText(config().isEnabled()));
            toggleItem.addActionListener(getEnableEventHandler());

            popupMenu.add(showItem);
            popupMenu.addSeparator();
            popupMenu.add(currentStateItem);
            popupMenu.add(toggleItem);
            popupMenu.addSeparator();
            popupMenu.add(closeItem);
            BufferedImage image = ImageIO.read(getClass().getResource("/icon/icon" + (int)tray.getTrayIconSize().getWidth() + ".png"));
            trayIcon = new TrayIcon(image, "Mini Proxy");
            trayIcon.addActionListener(getOpenEventHandler());
            trayIcon.setPopupMenu(popupMenu);
            tray.add(trayIcon);
        }
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
            if(toggleItem.getLabel().equals("Enable")) {
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
        return "Proxy mode: " + (enabled ? "Auto" : "Disabled");
    }

    private String getToggleText(boolean enabled) {
        return enabled ? "Disable" : "Enable";
    }

    @Subscribe
    public void setModeEvent(SetModeEvent event) {
        currentStateItem.setLabel(getStateText(event.mode == ProxyMode.AUTO));
        toggleItem.setLabel(getToggleText(event.mode == ProxyMode.AUTO));
    }
}
