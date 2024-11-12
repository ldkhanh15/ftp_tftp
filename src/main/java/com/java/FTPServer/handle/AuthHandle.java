package com.java.FTPServer.handle;

import com.java.FTPServer.system.UserSession;

import java.io.PrintWriter;

public interface AuthHandle {
    void handleUser(String username, PrintWriter out, UserSession userSession);
    void handlePass(String password, PrintWriter out, UserSession userSession);
    void handleQuit(PrintWriter out);
}