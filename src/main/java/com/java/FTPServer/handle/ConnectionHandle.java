package com.java.FTPServer.handle;

import com.java.FTPServer.system.UserSession;

import java.io.PrintWriter;

public interface ConnectionHandle {
    void processActiveMode(String clientConnectionData, PrintWriter out);
    void processPassiveMode(PrintWriter out, int dataPort, boolean isExtended);
    void processTypeTransfer(String typeTransfer, PrintWriter out, UserSession userSession);
}
