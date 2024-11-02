// CommandHandler.java
package com.java.FTPServer.handle;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public interface CommandHandler {
    void upload( Socket controlSocket,
                String fileName) throws Exception;
    void download(Socket controlSocket,
                  String fileName) throws Exception;
     void logout(DataInputStream dataInputStream, DataOutputStream dataOutputStream,
                             Socket controlSocket) throws Exception;
}
