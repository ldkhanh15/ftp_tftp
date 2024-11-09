package com.java.FTPServer;

import com.java.FTPServer.enums.TransferType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client extends Thread {
    private String currDirectory;
    //control connection
    private Socket controlSocket;
    private PrintWriter controlOutWriter;
    private BufferedReader controlIn;
    private int dataPort;
    private TransferType transferMode = TransferType.ASCII;
    private Router router;

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
            e.printStackTrace();
        } finally {
            try {
                controlIn.close();
                controlOutWriter.close();
                controlSocket.close();
                printOutput("Sockets closed and worker stopped");
            } catch (IOException e) {
                e.printStackTrace();
                printOutput("Could not close sockets");
            }
        }
    }
    private void printOutput(String msg) {
        System.out.println(msg);
    }
    public void sendMsgToClient(String msg) {
        controlOutWriter.println(msg);
    }
}
