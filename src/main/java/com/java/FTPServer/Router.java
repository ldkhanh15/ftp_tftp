package com.java.FTPServer;

import com.java.FTPServer.enums.Command;
import com.java.FTPServer.enums.ResponseCode;
import com.java.FTPServer.handle.*;
import com.java.FTPServer.system.UserSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;

@Component
@RequiredArgsConstructor
@Slf4j
public class Router {
    private PrintWriter controlOutWriter;

    private final AuthHandle authHandle;
    private final ConnectionHandle connectionHandle;
    private final FileHandle fileHandle;
    private final DirectoryHandle directoryHandle;
    private final CommonHandle commonHandle;

    public void executeCommand(String command, PrintWriter controlOutWriter, UserSession userSession) {

        this.controlOutWriter = controlOutWriter;

        String[] commands;
        commands = command.split(" ");
        Command commandType = Command.fromString(commands[0]);
        if (commandType == null) {
            sendMsgToClient(ResponseCode.NOT_IMPLEMENTED.getResponse());
            return;
        }

        log.info("Command: {}", commands[0]);
        if(commands.length > 1){
            log.info("Args: {}", commands[1]);
        }
        log.info("=======end=======");

        System.out.println("Command: " + commands[0]);
        if (commands.length > 1) {
            System.out.println("Args: " + commands[1]);
        }
        System.out.println("=======end=======");


        switch (commandType) {
            // Auth
            case USER:
                authHandle.handleUser(commands[1], controlOutWriter, userSession);
                break;
            case PASS:
                authHandle.handlePass(commands[1], controlOutWriter, userSession);
                break;
            case QUIT:
                authHandle.handleQuit(controlOutWriter);

            // Connection
            case PORT, EPRT:
                connectionHandle.processActiveMode(commands[1], controlOutWriter);
                break;
            case EPSV:
                connectionHandle.processPassiveMode(controlOutWriter, userSession.getDataPort());
                break;
            case TYPE:
                connectionHandle.processTypeTransfer(commands[1], controlOutWriter, userSession);
                break;

            // File
            case STOR:
                fileHandle.uploadFile(commands[1], controlOutWriter, userSession);
                break;
            case RETR:
                fileHandle.downloadFile(commands[1], controlOutWriter, userSession);
                break;
            case APPE:
                fileHandle.appendToFile(commands[1], controlOutWriter, userSession);
                break;
            case DELE:
                fileHandle.deleteFile(commands[1], controlOutWriter, userSession);
                break;

            // Directory
            case CWD:
                directoryHandle.changeWorkingDirectory(commands[1], controlOutWriter);
                break;
            case XPWD:
                directoryHandle.printWorkingDirectory(controlOutWriter, userSession.getCurrDirectory());
                break;
            case XMKD:
                directoryHandle.createDirectory(commands[1], controlOutWriter);
                break;
            case XRMD:
                directoryHandle.removeDirectory(commands[1], controlOutWriter);
                break;

            // Common
            case LIST:
                commonHandle.listDetail(controlOutWriter);
                break;
            case NLST:
                commonHandle.listName(controlOutWriter, userSession.getCurrDirectory());
                break;
            case RNFR:
                commonHandle.initiateRename(commands[1], controlOutWriter);
                break;
            case RNTO:
                commonHandle.finalizeRename(commands[1], controlOutWriter);
                break;

            default:
                sendMsgToClient(ResponseCode.NOT_IMPLEMENTED.getResponse());
                break;
        }
    }

    private void sendMsgToClient(String msg) {
        controlOutWriter.println(msg);
    }
}