package com.java.FTPServer1.handle;

import java.io.PrintWriter;

public interface CommonHandle {
    void listName(PrintWriter out);
    void listDetail(PrintWriter out);
    void initiateRename(String nameOnServer, PrintWriter out);
    void finalizeRename(String newName,PrintWriter out);
}
