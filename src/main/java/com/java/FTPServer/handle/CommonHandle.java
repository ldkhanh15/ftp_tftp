package com.java.FTPServer.handle;

import java.io.IOException;
import java.io.PrintWriter;

public interface CommonHandle {
    void listName(PrintWriter out, String currentDirectory);
    void listDetail(PrintWriter out, String currentDirectory) throws IOException;
    void initiateRename(PrintWriter out , String currentDirectory,String nameOnServer);
    void finalizeRename(PrintWriter out , String currentDirectory,String newName);
}
