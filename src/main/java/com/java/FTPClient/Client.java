package com.java.FTPClient;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class Client extends JFrame {
    private static final String SERVER = "localhost";
    private static final int PORT = 21;

    private JTextField downloadFileField;
    private JTextArea logArea;

    public Client() {
        setupGUI();
    }

    private void setupGUI() {
        setTitle("FTP Client");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(3, 1));

        // Upload Section
        JPanel uploadPanel = new JPanel(new BorderLayout());
        JButton uploadButton = new JButton("Select and Upload File");
        uploadButton.addActionListener(e -> uploadFile());
        uploadPanel.add(uploadButton, BorderLayout.CENTER);

        // Download Section
        JPanel downloadPanel = new JPanel(new BorderLayout());
        downloadFileField = new JTextField("desktop.ini");
        JButton downloadButton = new JButton("Download File");
        downloadButton.addActionListener(e -> downloadFile());
        downloadPanel.add(new JLabel("File Name to Download:"), BorderLayout.NORTH);
        downloadPanel.add(downloadFileField, BorderLayout.CENTER);
        downloadPanel.add(downloadButton, BorderLayout.EAST);
        // User List Section
        JButton getUsersButton = new JButton("Get Users List");
        getUsersButton.addActionListener(e -> requestUserList());
        panel.add(getUsersButton);
        // Log Area
        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(logArea);

        panel.add(uploadPanel);
        panel.add(downloadPanel);
        add(panel, BorderLayout.NORTH);
        add(logScrollPane, BorderLayout.CENTER);

        setVisible(true);
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> logArea.append(message + "\n"));
    }
    private void requestUserList() {
        try (Socket socket = new Socket(SERVER, PORT);
             DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
             DataInputStream dataInputStream = new DataInputStream(socket.getInputStream())) {

            // Send GET_USERS command
            dataOutputStream.writeUTF("GET_USERS");

            // Read the user list
            int userCount = dataInputStream.readInt();
            log("Number of users: " + userCount);

            for (int i = 0; i < userCount; i++) {
                String username = dataInputStream.readUTF();
                log("User: " + username);
                // Add more fields if needed
            }
        } catch (IOException e) {
            log("Error retrieving user list: " + e.getMessage());
        }
    }
    private void uploadFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select File to Upload");
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            try (Socket socket = new Socket(SERVER, PORT);
                 DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                 FileInputStream fileInputStream = new FileInputStream(file)) {
                log("Connected to FTP server for uploading.");

                // Send upload command and file name
                dataOutputStream.writeUTF("STOR "+file.getName());
//                dataOutputStream.writeUTF("DOWNLOAD");
//                dataOutputStream.writeUTF(file.getName());
                // Send file content
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    dataOutputStream.write(buffer, 0, bytesRead);
                }

                log("File uploaded to server successfully.");
            } catch (IOException e) {
                log("Upload error: " + e.getMessage());
            }
        } else {
            log("File upload canceled.");
        }
    }

    private void downloadFile() {
        String fileName = downloadFileField.getText();
        if (fileName.isEmpty()) {
            log("Please enter a file name to download.");
            return;
        }

        String userHome = System.getProperty("user.home");
        File downloadFolder = new File(userHome, "Downloads");
        File downloadedFile = new File(downloadFolder, "downloaded_" + fileName);

        try (Socket socket = new Socket(SERVER, PORT);
             DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
             DataInputStream dataInputStream = new DataInputStream(socket.getInputStream())) {

            log("Connected to FTP server for downloading.");

            // Send download command and file name
            dataOutputStream.writeUTF("RETR " + fileName);

            // Check server response for file availability
            String serverResponse = dataInputStream.readUTF();
            log("Server response: " + serverResponse);

            if ("FILE_FOUND".equals(serverResponse)) {
                try (FileOutputStream fileOutputStream = new FileOutputStream(downloadedFile)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = dataInputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, bytesRead);
                    }
                }
                log("File downloaded successfully as " + downloadedFile.getAbsolutePath());
            } else {
                log("File not found on server.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            log("Download error: " + e.getMessage());
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(Client::new);
    }
}
