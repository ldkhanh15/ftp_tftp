package old.FTPServer;

import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import old.FTPServer.enums.TransferType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

@ToString
@Slf4j
public class Client extends Thread {
    @Setter
    private String currDirectory;
    private final Socket controlSocket;
    private PrintWriter controlOutWriter;
    private BufferedReader controlIn;
    private final int dataPort;
    private final TransferType transferMode = TransferType.ASCII;
    private final Router router;

    public Client(Socket client, int dataPort) {
        super();
        this.controlSocket = client;
        this.dataPort = dataPort;
        this.currDirectory = System.getProperty("user.dir") + "/ftp_root";
        router=new Router();
    }

    public void run() {
        printOutput("Current working directory " + this.currDirectory);
        try {
            controlIn = new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));
            controlOutWriter = new PrintWriter(controlSocket.getOutputStream(), true);
            sendMsgToClient("Welcome to the FTP-Server");
            while (true) {
                router.executeCommand(controlIn.readLine(),controlOutWriter,dataPort);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            try {
                controlIn.close();
                controlOutWriter.close();
                controlSocket.close();
                printOutput("Sockets closed and worker stopped");
            } catch (IOException e) {
                printOutput("Could not close sockets");
            }
        }
    }
    private void printOutput(String msg) {
        log.info(msg);
    }
    public void sendMsgToClient(String msg) {
        controlOutWriter.println(msg);
    }
}
