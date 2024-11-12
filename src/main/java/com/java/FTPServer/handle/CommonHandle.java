package com.java.FTPServer.handle;

import java.io.PrintWriter;

public interface CommonHandle {
    void listName(PrintWriter out, String currentDirectory);
    void listDetail(PrintWriter out, String currentDirectory);
    void initiateRename(String nameOnServer, PrintWriter out);
    void finalizeRename(String newName,PrintWriter out);
}
