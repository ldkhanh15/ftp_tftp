package com.java.FTPServer;

import com.java.GUI.view.DirectionTreeView;
import com.java.GUI.view.LoginView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
@Component
public class Server {
    private static final int PORT = ConstFTP.PORT;
    private final Router router;

    public Server(){
        router=new Router();
    }
    private ServerSocket serverSocket;
    boolean serverRunning = true;
    private List<Client> clients;

    public void start() {
        clients = new ArrayList<>();
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            System.out.println("Could not create server socket");
            System.exit(-1);
        }

        System.out.println("FTP Server started listening on port " + PORT);
        while (serverRunning) {

            try {
                Socket socketClient = serverSocket.accept();
                // 1 client 1 port data, mo tu 1023
                int dataPort = PORT + clients.size() + 1022;
                Client client = new Client(socketClient, dataPort);
                clients.add(client);
                System.out.println("New connection received. Worker was created.");
                client.start();
            } catch (IOException e) {
                System.out.println("Exception encountered on accept");
                e.printStackTrace();
            }

        }
        try {
            serverSocket.close();
            System.out.println("Server was stopped");

        } catch (IOException e) {
            System.out.println("Problem stopping server");
            System.exit(-1);
        }
    }
}
