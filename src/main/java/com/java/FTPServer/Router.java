package com.java.FTPServer;

import com.java.FTPServer.enums.Command;
import com.java.FTPServer.enums.ResponseCode;
import com.java.FTPServer.handle.HandleCommand;
import com.java.controller.UserController;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.net.Socket;

@Component
@RequiredArgsConstructor
public class Router {
    private final HandleCommand handleCommand;
    private String temporaryUsername;
    public void routeCommand(String command, PrintWriter out, Socket controlSocket) {
        String[] commandParts = command.split(" ", 2);
        Command commandType = Command.fromString(commandParts[0]);
        if (commandType == null) {
            out.println(ResponseCode.NOT_IMPLEMENTED);
            return;
        }

        switch (commandType) {
            case STOR:
                if (commandParts.length > 1) {
                    handleCommand.storeFile(commandParts[1], controlSocket, out);

                } else {
                    out.println(ResponseCode.NOT_SUPPORTED);
                }
                break;

            case USER:
                if (commandParts.length > 1) {
                    temporaryUsername = commandParts[1];
                    out.println(ResponseCode.NEED_PASSWORD);
                } else {
                    out.println(ResponseCode.NOT_SUPPORTED);
                }
                break;

            case PASS:
                if (commandParts.length > 1 && temporaryUsername != null) {
                    handleCommand.login(commandParts, temporaryUsername, out);
                } else {
                    out.println(ResponseCode.NOT_SUPPORTED);
                }
                break;

            default:
                out.println(ResponseCode.NOT_IMPLEMENTED);
                break;
        }
    }
}