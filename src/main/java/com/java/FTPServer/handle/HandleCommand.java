package com.java.FTPServer.handle;

import java.io.PrintWriter;
import java.net.Socket;

public interface HandleCommand {
    void storeFile(String filePath, Socket dataSocket, PrintWriter out);
    void login(String[] commandParts, String username, PrintWriter out);
}
