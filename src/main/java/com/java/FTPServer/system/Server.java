package com.java.FTPServer.system;

import com.java.FTPServer.GUI.MainGUI;
import com.java.FTPServer.ulti.LogHandler;
import com.java.FTPServer.ulti.UserStore;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class Server {
    private static final int PORT = ConstFTP.PORT;
    private final ApplicationContext applicationContext;
    private List<Client> clients = new ArrayList<>();
    private final List<Integer> dataPorts=new ArrayList<>();
    private ServerSocket serverSocket;
    boolean serverRunning = true;
    private MainGUI mainGUI;
    public void start() {
        mainGUI=new MainGUI();
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            LogHandler.write("logs/servers","error.txt","Could not create server socket {}"+ e.getMessage(),e);
            log.error("Could not create server socket {}", e.getMessage());
            System.exit(-1);
        }
        LogHandler.write("logs/servers","error.txt","FTP Server started listening on port " + PORT);
        log.info("FTP Server started listening on port " + PORT);
        while (serverRunning) {
            try {
                Socket socketClient = serverSocket.accept();
                int dataPort = getAvailableDataPort();
                System.out.println("So client: "+clients.size());
                // Create a new Client instance for each incoming connection
                Client client = applicationContext.getBean(Client.class);
                client.init(socketClient, dataPort);
                UserStore.addClient(client);
                log.info("New connection received. Worker was created: {}", client.toString());
                LogHandler.write("logs/servers","error.txt",
                        "New connection received. Worker was created: {}"+ client.toString());
                // Start a new thread for the client
                Thread clientThread = new Thread(client);
                clientThread.start();
            } catch (IOException e) {
                log.error("Exception encountered on accept: {}", e.getMessage());
                LogHandler.write("logs/servers","error.txt",
                        "Exception encountered on accept: {}"+ e.getMessage(),e);
            }
        }

        try {
            serverSocket.close();
            LogHandler.write("logs/servers","error.txt",
                    "Server was stopped");
            log.info("Server was stopped");
        } catch (IOException e) {
            log.error("Problem stopping server : {}", e.getMessage());
            LogHandler.write("logs/servers","error.txt",
                    "Problem stopping server : {}"+ e.getMessage(),e);
            System.exit(-1);
        }
    }
    private int getAvailableDataPort() {
        int dataPort;
        while (true) {
            dataPort = 1024 + (int)(Math.random() * (65535 - 1024));

            if (!dataPorts.contains(dataPort)) {
                dataPorts.add(dataPort);
                break;
            }
        }
        return dataPort;
    }
}
