package com.java.FTPServer1.system;

import com.java.FTPServer1.Router;
import com.java.FTPServer1.enums.ResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

@ToString
@Slf4j
@Component
@RequiredArgsConstructor
public class Client extends Thread {
    private Socket controlSocket;
    private final Router router;
    private UserSession userSession;

    public void init(Socket clientSocket, int dataPort) {
        this.controlSocket = clientSocket;
        userSession = new UserSession();
        userSession.setDataPort(dataPort);
    }

    public void run() {

        PrintWriter controlOutWriter = null;
        BufferedReader controlIn = null;
        try {
            controlIn = new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));
            controlOutWriter = new PrintWriter(controlSocket.getOutputStream(), true);
            controlOutWriter.println(ResponseCode.SERVICE_READY.getResponse("Welcome to FTP server"));
            while (true) {
                router.executeCommand(controlIn.readLine(),controlOutWriter, userSession);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            try {
                if (controlIn != null) {
                    controlIn.close();
                }
                if (controlOutWriter != null) {
                    controlOutWriter.close();
                }
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
}
