package old.FTPServer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class Server {
    private static final int PORT = ConstFTP.PORT;

    private ServerSocket serverSocket;
    boolean serverRunning = true;

    public void start() {
        List<Client> clients = new ArrayList<>();
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
                int dataPort = PORT + clients.size() + 1022;
                Client client = new Client(socketClient, dataPort);
                clients.add(client);
                log.info("New connection received. Worker was created.");
                client.start();
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
}
