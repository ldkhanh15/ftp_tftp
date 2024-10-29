package com.java.FTPServer;

import com.java.FTPServer.enums.Command;
import com.java.GUI.view.LoginView;
import com.java.Service.ItemService;
import com.java.controller.UserController;
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
    private static final int PORT = 21;
    private final Router router;
    private final LoginView loginView;
    public void start() {
        javax.swing.SwingUtilities.invokeLater(() -> {
            loginView.setVisible(true);
        });

        ExecutorService executor = Executors.newFixedThreadPool(10); // Tạo thread pool
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("FTP Server started on port " + PORT);
            File rootDirectory = new File(Const.ROOT_DIR);
            if (!rootDirectory.exists()) {
                rootDirectory.mkdirs();  // Tạo thư mục nếu không tồn tại
                System.out.println("Created directory: " + Const.ROOT_DIR);
            }
            while (true) {
                Socket clientSocket = serverSocket.accept(); // Chấp nhận kết nối mới
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                executor.submit(new ClientHandler(clientSocket, router)); // Gửi ClientHandler vào executor
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown(); // Đóng executor khi server dừng
        }
    }
    private static class ClientHandler implements Runnable {
        private final Socket controlSocket; // Kết nối của client
        private final BufferedReader in; // Đọc dữ liệu từ client
        private final PrintWriter out; // Gửi dữ liệu đến client
        private final Router router; // Router để xử lý lệnh

        public ClientHandler(Socket controlSocket, Router router) throws IOException {
            this.controlSocket = controlSocket;
            this.router = router;
            this.in = new BufferedReader(new InputStreamReader(controlSocket.getInputStream())); // Khởi tạo BufferedReader
            this.out = new PrintWriter(controlSocket.getOutputStream(), true); // Khởi tạo PrintWriter
        }

        @Override
        public void run() {
            try {
                out.println("220 Welcome to Simple FTP Server"); // Gửi thông báo chào mừng

                String command;
                while ((command = in.readLine()) != null) {
                    System.out.println("Received command: " + command); // In lệnh nhận được
                    router.routeCommand(command, out, controlSocket); // Chuyển lệnh cho router xử lý
                    if (command.equalsIgnoreCase(Command.QUIT.toString())) { // Kiểm tra lệnh QUIT
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    controlSocket.close(); // Đóng kết nối khi hoàn tất
                    System.err.println("Client disconnected");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
