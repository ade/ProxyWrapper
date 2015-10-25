package se.ade.autoproxywrapper.gui.controller;

import javafx.application.Platform;
import se.ade.autoproxywrapper.Main;
import se.ade.autoproxywrapper.events.EventBus;
import se.ade.autoproxywrapper.events.ShutDownEvent;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class SystemTrayIcon {

    private TrayIcon trayIcon;
    private Main application;

    public SystemTrayIcon(Main main) throws IOException, AWTException {
        this.application = main;
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();

            PopupMenu popupMenu = new PopupMenu();
            MenuItem showItem = new MenuItem("Open Mini Proxy");
            showItem.addActionListener(getOpenEventHandler());

            MenuItem closeItem = new MenuItem("Close");
            closeItem.addActionListener(getCloseEventHandler());
            popupMenu.add(showItem);
            popupMenu.add(closeItem);
            BufferedImage image = ImageIO.read(getClass().getResource("/assets/icon16.png"));
            trayIcon = new TrayIcon(image, "Mini Proxy");
            trayIcon.addActionListener(getOpenEventHandler());
            trayIcon.setPopupMenu(popupMenu);
            tray.add(trayIcon);
        }
    }

    private ActionListener getOpenEventHandler() {
        return event -> {
            Platform.runLater(() -> {
                application.getPrimaryStage().setIconified(false);
                application.getPrimaryStage().requestFocus();
            });
        };
    }

    private ActionListener getCloseEventHandler() {
        return event -> {
            Platform.runLater(() -> {
                EventBus.get().post(new ShutDownEvent());
            });
        };
    }

}
