package se.ade.autoproxywrapper;

import com.apple.eawt.Application;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class HorribleJavaGui extends Frame implements ActionListener {
    private enum Action {
        Quit
    }

    TextArea statusText;
    TextArea logText;

    public HorribleJavaGui() {
        initIcon();

        setLayout(new BorderLayout(10, 10));

        statusText = new TextArea("", 2, 35, TextArea.SCROLLBARS_NONE);
        statusText.setText("StatusText");
        statusText.setEditable(false);
        statusText.setBackground(Color.green);
        add(statusText, BorderLayout.NORTH);

        logText = new TextArea("Horrible Java GUI Engaged!");
        logText.setFont(new Font("Courier New", 0, 12));

        add(logText);

        Panel bpanel = new Panel(new FlowLayout());

        Choice mode = new Choice();
        mode.add("Auto");
        mode.add("Direct");
        mode.add("Forward proxy");
        mode.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                onSelectMode(e.getItem().toString());
            }
        });
        bpanel.add(mode);

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
    }

    void quit() {
        setVisible(false);
        System.exit(0);
    }

    private void onSelectMode(String s) {
        logMessage("Using mode: " + s);
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
        logText.append(message + "\n");
        logText.setCaretPosition(logText.getText().length());
    }

    private void initIcon() {
        Image iconimage;
        String iconPath = "icon.png";

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