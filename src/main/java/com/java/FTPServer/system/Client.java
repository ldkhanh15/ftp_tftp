package com.java.FTPServer.system;

import com.java.FTPServer.Router;
import com.java.FTPServer.enums.ResponseCode;
import com.java.FTPServer.ulti.LogHandler;
import com.java.FTPServer.ulti.UserSessionManager;
import com.java.FTPServer.ulti.UserStore;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Objects;

@ToString
@Slf4j
@Component
@RequiredArgsConstructor
@Getter
@Setter
@Scope("prototype")
public class Client extends Thread {
    private Socket controlSocket;
    private final Router router;
    private UserSession userSession;
    private int dataPort;
    public void init(Socket clientSocket, int dataPort) {
        this.dataPort=dataPort;
        this.controlSocket = clientSocket;

    }

    public void run() {
        UserSession userSession = new UserSession();
        userSession.setDataPort(dataPort);
        this.userSession = userSession;
        UserStore.addClient(this);
        UserSessionManager.setUserSession(userSession);
        while (!controlSocket.isClosed()) {
            PrintWriter controlOutWriter = null;
            BufferedReader controlIn = null;
            try {
                controlIn = new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));
                controlOutWriter = new PrintWriter(controlSocket.getOutputStream(), true);
                controlOutWriter.println(ResponseCode.SERVICE_READY.getResponse("Welcome to FTP server"));
                while (true) {
                    router.executeCommand(this,controlIn.readLine(),controlOutWriter,
                            UserSessionManager.getUserSession());
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
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Client client = (Client) obj;
        return this.getUserSession() != null &&
                this.getUserSession().getDataPort() == client.getUserSession().getDataPort() &&
                Objects.equals(this.controlSocket, client.controlSocket); // So sánh thêm socket
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                this.getUserSession() != null ? this.getUserSession().getDataPort() : 0,
                this.controlSocket
        );
    }

}
