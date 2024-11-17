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
        System.out.println(userSession.getCurrDirectory());
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
                connectionHandle.processPassiveMode(controlOutWriter, userSession.getDataPort(), true);
                break;
            case PASV:
                connectionHandle.processPassiveMode(controlOutWriter, userSession.getDataPort(), false);
            case TYPE:
                connectionHandle.processTypeTransfer(commands[1], controlOutWriter, userSession);
                break;

            // File
            case STOR:
                fileHandle.uploadFile( controlOutWriter,commands[1], userSession);
                break;
            case RETR:
                fileHandle.downloadFile(controlOutWriter,commands[1],  userSession);
                break;
            case APPE:
                fileHandle.appendToFile( controlOutWriter, commands[1],userSession);
                break;
            case DELE:
                fileHandle.deleteFile( controlOutWriter, commands[1],userSession);
                break;

            // Directory
            case CWD:
                directoryHandle.changeWorkingDirectory(controlOutWriter,commands[1], userSession);
                break;
            case XPWD:
                directoryHandle.printWorkingDirectory(controlOutWriter, userSession.getCurrDirectory());
                break;
            case XMKD:
                directoryHandle.createDirectory( controlOutWriter,commands[1], userSession.getCurrDirectory());
                break;
            case XRMD:
                directoryHandle.removeDirectory( controlOutWriter,commands[1], userSession.getCurrDirectory());
                break;

            // Common
            case LIST:
                commonHandle.listDetail(controlOutWriter, userSession.getCurrDirectory());
                break;
            case NLST:
                commonHandle.listName(controlOutWriter, userSession.getCurrDirectory());
                break;
            case RNFR:
                commonHandle.initiateRename(controlOutWriter,commands[1]);
                break;
            case RNTO:
                commonHandle.finalizeRename(controlOutWriter,commands[1]);
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