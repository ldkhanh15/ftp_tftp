package com.java.FTPServer;

import com.java.FTPServer.enums.Command;
import com.java.FTPServer.enums.ResponseCode;
import com.java.FTPServer.enums.UserStatus;
import com.java.FTPServer.handle.*;
import com.java.FTPServer.system.Client;
import com.java.FTPServer.system.UserSession;
import com.java.FTPServer.ulti.LogHandler;
import com.java.FTPServer.ulti.UserSessionManager;
import com.java.FTPServer.ulti.UserStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
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

    public void executeCommand(Client client, String command, PrintWriter controlOutWriter, UserSession userSession) {
        this.controlOutWriter = controlOutWriter;
        String[] commands;
        commands = command.split(" ");
        Command commandType = Command.fromString(commands[0]);
        if (commandType == null) {
            sendMsgToClient(ResponseCode.NOT_IMPLEMENTED.getResponse());
            return;
        }
        StringBuilder path = new StringBuilder();
        for(int i=1;i<commands.length;i++){
            if (i == command.length() - 1)
                path.append(commands[i]);
            else
                path.append(commands[i]).append(" ");
        }
       if(UserSessionManager.getUserSession() !=null && UserSessionManager.getUserSession().getStatus()==
               UserStatus.LOGGED_IN){
           String log="Command: {}"+commands[0]+"\n";
           if(commands.length > 1){
               log+="Args: {}"+ commands[1]+"\n";
           }
           log+="=======end=======";
           LogHandler.write("logs/users",UserSessionManager.getUserSession().getUsername()+".txt",
                   log);
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
                UserSessionManager.setUserSession(null);
                UserStore.removeClient(client);
                break;

            // Connection
            case PORT, EPRT:
                connectionHandle.processActiveMode(path.toString(), controlOutWriter);
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
                fileHandle.uploadFile( controlOutWriter,path.toString(), userSession);
                break;
            case RETR:
                fileHandle.downloadFile(controlOutWriter,path.toString(),  userSession);
                break;
            case APPE:
                fileHandle.appendToFile( controlOutWriter, path.toString(),userSession);
                break;
            case DELE:
                fileHandle.deleteFile( controlOutWriter, path.toString(),userSession);
                break;

            // Directory
            case CWD:
                directoryHandle.changeWorkingDirectory(controlOutWriter,path.toString(), userSession);
                break;
            case XPWD:
                directoryHandle.printWorkingDirectory(controlOutWriter, userSession.getCurrDirectory());
                break;
            case XMKD:
                directoryHandle.createDirectory( controlOutWriter,path.toString(), userSession.getCurrDirectory());
                break;
            case XRMD:
                directoryHandle.removeDirectory( controlOutWriter,path.toString(), userSession.getCurrDirectory());
                break;

            // Common
            case LIST:
                try {
                    String pathUp = userSession.getCurrDirectory();
                    if(commands.length>1){
                        pathUp += "/"+path;
                    }
                    commonHandle.listDetail(controlOutWriter,pathUp);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            case NLST:
                commonHandle.listName(controlOutWriter, userSession.getCurrDirectory());
                break;
            case RNFR:
                commonHandle.initiateRename(controlOutWriter, userSession.getCurrDirectory(),path.toString());
                break;
            case RNTO:
                commonHandle.finalizeRename(controlOutWriter, userSession.getCurrDirectory(),path.toString());
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