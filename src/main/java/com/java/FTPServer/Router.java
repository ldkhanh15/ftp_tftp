package com.java.FTPServer;

import com.java.FTPServer.enums.Command;
import com.java.FTPServer.enums.ResponseCode;
import com.java.FTPServer.handle.CommandHandler;
import com.java.controller.UserController;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.Socket;

@Component
@RequiredArgsConstructor
public class Router {
    private final CommandHandler commandHandler;
    private String temporaryUsername;
    public void routeCommand(String command, Socket controlSocket) throws IOException {
       try(DataOutputStream dataOutputStream=new DataOutputStream(controlSocket.getOutputStream());
       DataInputStream dataInputStream = new DataInputStream(controlSocket.getInputStream())){

           String cmds[]=command.split(" ");
           Command commandType = Command.fromString(cmds[0]);
           if (commandType == null) {
               dataOutputStream.writeUTF(ResponseCode.NOT_IMPLEMENTED.getResponse());
               return;
           }
           switch (commandType) {
               case STOR:
                   commandHandler.upload(controlSocket,cmds[1]);
                   break;
               case RETR:
                   commandHandler.download(controlSocket,cmds[1]);
                   break;
//            case USER:
//                if (commandParts.length > 1) {
//                    temporaryUsername = commandParts[1];
//                    out.println(ResponseCode.NEED_PASSWORD);
//                } else {
//                    out.println(ResponseCode.NOT_SUPPORTED);
//                }
//                break;
//
//            case PASS:
//                if (commandParts.length > 1 && temporaryUsername != null) {
//                    handleCommand.login(commandParts, temporaryUsername, out);
//                } else {
//                    out.println(ResponseCode.NOT_SUPPORTED);
//                }
//                break;

               default:
                   dataOutputStream.writeUTF(ResponseCode.NOT_IMPLEMENTED.getResponse());
                   break;
           }
       } catch (IOException e) {
           throw new RuntimeException(e);
       } catch (Exception e) {
           throw new RuntimeException(e);
       }
    }
}