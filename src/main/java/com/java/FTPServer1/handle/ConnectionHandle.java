package com.java.FTPServer1.handle;

import java.io.PrintWriter;

public interface ConnectionHandle {
    void processActiveMode(String clientConnectionData, PrintWriter out);
    void processPassiveMode(PrintWriter out, int dataPort);
    void processTypeTransfer(String typeTransfer, PrintWriter out);
}
