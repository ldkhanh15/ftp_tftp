package com.java.FTPServer.system;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
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
    private final List<Client> clients = new ArrayList<>();
    private final List<Integer> dataPorts=new ArrayList<>();
    private ServerSocket serverSocket;
    boolean serverRunning = true;

    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            log.error("Could not create server socket {}", e.getMessage());
            System.exit(-1);
        }

        log.info("FTP Server started listening on port " + PORT);
        while (serverRunning) {
            try {
                Socket socketClient = serverSocket.accept();
                int dataPort = getAvailableDataPort();
                System.out.println("So client: "+clients.size());
                // Create a new Client instance for each incoming connection
                Client client = applicationContext.getBean(Client.class);
                client.init(socketClient, dataPort);
                clients.add(client);

                log.info("New connection received. Worker was created: {}", client.toString());

                // Start a new thread for the client
                Thread clientThread = new Thread(client);
                clientThread.start();
            } catch (IOException e) {
                log.error("Exception encountered on accept: {}", e.getMessage());
                log.error(e.getMessage());
            }
        }

        try {
            serverSocket.close();
            log.info("Server was stopped");
        } catch (IOException e) {
            log.error("Problem stopping server : {}", e.getMessage());
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
