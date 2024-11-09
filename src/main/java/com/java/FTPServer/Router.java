package com.java.FTPServer;

import com.java.FTPServer.enums.Command;
import com.java.FTPServer.enums.ResponseCode;
import com.java.FTPServer.handle.CommandHandler;
import com.java.FTPServer.handle.CommandHandlerImpl;
import com.java.controller.UserController;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.Socket;

public class Router {
    private final CommandHandlerImpl commandHandler;
    private PrintWriter controlOutWriter;
    public Router(){
        this.commandHandler=new CommandHandlerImpl();
    }
    public void executeCommand(String c,PrintWriter controlOutWriter,int dataPort) {
        this.controlOutWriter=controlOutWriter;
        commandHandler.setDataPort(dataPort);
        String cmds[]=c.split(" ");
        Command commandType = Command.fromString(cmds[0]);
        if (commandType == null) {
            sendMsgToClient(ResponseCode.NOT_IMPLEMENTED.getResponse());
            return;
        }
        printOutput("Command: " + cmds[0]);
        if(cmds.length>1){
            printOutput("Args: "+cmds[1]);
        }
        switch (commandType) {
            case STOR:
                commandHandler.handleStor(cmds[1],controlOutWriter);
                break;
            case TYPE:
                commandHandler.handleType(cmds[1],controlOutWriter);
                break;
            case RETR:
                commandHandler.handleRetr(cmds[1],controlOutWriter);
                break;
            case PORT:
                commandHandler.handlePort(cmds[1],controlOutWriter);
                break;
            case PASV:
                commandHandler.handlePasv(controlOutWriter);
                break;
            default:
                sendMsgToClient("Unknown command");
                break;

        }

    }
    private void sendMsgToClient(String msg) {
        controlOutWriter.println(msg);
    }
    private void printOutput(String msg) {
        System.out.println(msg);
    }
}