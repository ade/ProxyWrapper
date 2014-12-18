package se.ade.autoproxywrapper;

import com.apple.eawt.Application;
import com.google.common.eventbus.Subscribe;
import se.ade.autoproxywrapper.events.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HorribleJavaGui extends Frame implements ActionListener {
    private enum Action {
        Quit
    }

    TextArea statusText;
    TextArea logText;
    MiniHttpProxy proxy;
    SimpleDateFormat sdf = new SimpleDateFormat("d/MM HH:mm:ss");

    private Object eventListener = new Object() {
        @Subscribe
        public void onEvent(ForwardProxyConnectionFailureEvent e) {
            logMessage(e.error.toString());
            //System.out.println("Forward proxy connection failed: " + throwable.toString());
        }

        @Subscribe
        public void onEvent(RequestEvent e) {
            logMessage(e.method + " " + e.url);
        }

        @Subscribe
        public void onEvent(DetectModeEvent e) {
            statusText.setText("In " + e.mode.getName() + " mode (auto)");
        }
    };

    public HorribleJavaGui() {
        initIcon();

        setLayout(new BorderLayout(10, 10));

        statusText = new TextArea("", 2, 35, TextArea.SCROLLBARS_NONE);
        statusText.setText("StatusText");
        statusText.setEditable(false);
        statusText.setBackground(Color.green);
        add(statusText, BorderLayout.NORTH);

        logText = new TextArea();
        logText.setFont(new Font("Courier New", 0, 12));
        logText.setPreferredSize(new Dimension(1200, 400));

        add(logText);

        Panel bpanel = new Panel(new FlowLayout());

        final Checkbox enableLogging = new Checkbox("Log all requests");
        enableLogging.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                EventBus.get().post(new SetLoggingEnabledEvent(enableLogging.getState()));
            }
        });

        Choice modeChoice = new Choice();
        for(ProxyMode mode : ProxyMode.values()) {
            modeChoice.add(mode.getName());
        }
        bpanel.add(enableLogging);

        modeChoice.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                onSelectMode(ProxyMode.valueOf(e.getItem().toString()));
            }
        });
        //TODO make this work
        //bpanel.add(modeChoice);

        Button quitButton;

        quitButton = new Button("Shut down");
        quitButton.setActionCommand(Action.Quit.name());
        quitButton.addActionListener(this);
        bpanel.add(quitButton);

        add(bpanel, BorderLayout.SOUTH);

        addWindowListener(new WindowListenerAdapter() {
            @Override
            public void windowClosing(WindowEvent evt) {
                quit();
            }
        });

        pack();

        //Center window on screen
        setLocationRelativeTo(null);

        EventBus.get().register(eventListener);

        new Thread(new Runnable() {
            @Override
            public void run() {
                proxy = new MiniHttpProxy();
                proxy.startProxy();
            }
        }).start();

        logMessage("Started");
    }

    void quit() {
        EventBus.get().post(new ShutDownEvent());
        EventBus.get().unregister(eventListener);
        setVisible(false);
        System.exit(0);
    }

    private void onSelectMode(ProxyMode mode) {
        logMessage("Selected mode: " + mode.getName());
        EventBus.get().post(new SetModeEvent(mode));
    }

    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        Action action;
        try {
            action = Action.valueOf(command);
        } catch (IllegalArgumentException a) {
            return;
        }

        switch (action) {
            case Quit:
                quit();
                break;
        }
    }

    private void logMessage(String message) {
        logText.append(sdf.format(new Date()) + ": " + message + "\n");
        logText.setCaretPosition(logText.getText().length());
    }

    private void initIcon() {
        Image iconimage;
        String iconPath = "assets/icon512.png";

        InputStream iconStream = getClass().getResourceAsStream(iconPath);
        if(iconStream == null) {
            try {
                iconStream = new FileInputStream(iconPath);
            } catch (Exception e) {
                iconStream = null;
            }
        }

        if (iconStream != null) {
            try {
                iconimage = ImageIO.read(iconStream);
            } catch (IOException e) {
                return;
            }

            if (iconimage != null) {
                setIconImage(iconimage);
                Application application = Application.getApplication();
                application.setDockIconImage(iconimage);
            }
        }

    }
}