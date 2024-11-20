package com.java.FTPServer.system;

import com.java.FTPServer.Router;
import com.java.FTPServer.enums.ResponseCode;
import com.java.FTPServer.ulti.LogHandler;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
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
@Getter
@Setter
public class Client extends Thread {
    private Socket controlSocket;
    private final Router router;
    private UserSession userSession;

    public void init(Socket clientSocket, int dataPort) {
        this.controlSocket = clientSocket;
        UserSession userSession = new UserSession();
        userSession.setDataPort(dataPort);
        this.userSession=userSession;
    }

    public void run() {
        UserSessionContext.setUserSession(this.userSession);
        while (!controlSocket.isClosed()) {
            PrintWriter controlOutWriter = null;
            BufferedReader controlIn = null;
            try {
                controlIn = new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));
                controlOutWriter = new PrintWriter(controlSocket.getOutputStream(), true);
                controlOutWriter.println(ResponseCode.SERVICE_READY.getResponse("Welcome to FTP server"));
                while (true) {
                    router.executeCommand(controlIn.readLine(),controlOutWriter, UserSessionContext.getUserSession());
                }
            } catch (Exception e) {
                LogHandler.write("logs/servers","error.txt",
                         e.getMessage(),e);
            } finally {
                try {
                    if (controlIn != null) {
                        controlIn.close();
                    }
                    if (controlOutWriter != null) {
                        controlOutWriter.close();
                    }
                    controlSocket.close();
                    LogHandler.write("logs/servers","error.txt",
                            "Sockets closed and worker stopped");
                    printOutput("Sockets closed and worker stopped");
                } catch (IOException e) {
                    LogHandler.write("logs/servers","error.txt",
                            "Could not close sockets");
                    printOutput("Could not close sockets");
                }
            }
        }
    }

    private void printOutput(String msg) {
        log.info(msg);
    }
}
