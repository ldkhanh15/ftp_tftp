package com.java.FTPServer;

import com.java.FTPServer.enums.Command;
import com.java.GUI.view.DirectionTreeView;
import com.java.GUI.view.LoginView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@RequiredArgsConstructor
public class Server {
    private static final int PORT = ConstFTP.PORT;
    private final Router router;
    private final LoginView loginView;
    private final DirectionTreeView directionTreeView;

    public void start() {

        javax.swing.SwingUtilities.invokeLater(() -> {
            directionTreeView.setVisible(true);
        });

        ExecutorService executor = Executors.newFixedThreadPool(10);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("FTP Server started on port " + PORT);
            File rootDirectory = new File(ConstFTP.ROOT_DIR);
            if (!rootDirectory.exists()) {
                rootDirectory.mkdirs();  // Tạo thư mục nếu không tồn tại
                System.out.println("Created directory: " + ConstFTP.ROOT_DIR);
            }
            while (true) {
                Socket clientSocket = serverSocket.accept(); // Chấp nhận kết nối mới
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                executor.submit(new ClientHandler(clientSocket, router)); // Gửi ClientHandler vào executor
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    private static class ClientHandler implements Runnable {
        private final Socket controlSocket; // Client connection
        private final Router router; // Router for command handling
        private DataInputStream dataInputStream; // Input stream for the client
        private DataOutputStream dataOutputStream; // Output stream for the client

        public ClientHandler(Socket controlSocket, Router router) {
            this.controlSocket = controlSocket;
            this.router = router;
        }

        @Override
        public void run() {
            try {
                // Initialize streams outside the loop
                dataInputStream = new DataInputStream(controlSocket.getInputStream());
                dataOutputStream = new DataOutputStream(controlSocket.getOutputStream());
//                dataOutputStream.writeUTF("220 Welcome to Simple FTP Server"); // Send welcome message

                String command;
                while (true) {
                    try {
                        command = dataInputStream.readUTF(); // Read command from client
                        System.out.println("Received command: " + command);
                        router.routeCommand(command, controlSocket);

                        if (command.equalsIgnoreCase(Command.QUIT.toString())) {
                            break; // Exit the loop if QUIT command is received
                        }
                    } catch (EOFException e) {
                        System.out.println("Client disconnected unexpectedly.");
                        break; // Break on unexpected disconnect
                    } catch (IOException e) {
                        System.out.println("Error reading command: " + e.getMessage());
                        break; // Break on read errors
                    }
                }
            } catch (IOException e) {
                e.printStackTrace(); // Handle exceptions during stream initialization
            } finally {
                // Close resources in the finally block to ensure they are closed regardless of what happens
                try {
                    if (dataInputStream != null) dataInputStream.close();
                    if (dataOutputStream != null) dataOutputStream.close();
                    controlSocket.close(); // Close the socket when done
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
