package com.java.FTPServer.GUI;

import com.java.FTPServer.ulti.LogHandler;
import jakarta.persistence.Column;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
@Component
public class Log {
    // Create the log management panel
    public static JPanel createLogPanel() {
        JPanel logPanel = new JPanel();
        logPanel.setLayout(new BorderLayout());

        JTextArea logTextArea = new JTextArea(15, 50);
        logTextArea.setEditable(false);
        logTextArea.setText(LogHandler.read("logs/servers","error.txt"));
        JScrollPane logScrollPane = new JScrollPane(logTextArea);
        logPanel.add(logScrollPane, BorderLayout.CENTER);

        return logPanel;
    }
}
