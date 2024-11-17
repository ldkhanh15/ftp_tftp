package com.java.FTPServer.handle;

import java.io.PrintWriter;

public interface CommonHandle {
    void listName(PrintWriter out, String currentDirectory);
    void listDetail(PrintWriter out, String currentDirectory);
    void initiateRename(PrintWriter out,String nameOnServer);
    void finalizeRename(PrintWriter out,String newName);
}
