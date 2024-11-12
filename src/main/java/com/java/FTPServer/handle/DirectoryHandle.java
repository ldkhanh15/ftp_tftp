package com.java.FTPServer.handle;

import com.java.FTPServer.system.UserSession;

import java.io.PrintWriter;

public interface DirectoryHandle {
    void createDirectory(String directoryName, PrintWriter out, String currDirectory);
    void removeDirectory(String directoryName, PrintWriter out, String currDirectory);
    void changeWorkingDirectory(String directoryName, PrintWriter out, UserSession userSession);
    void printWorkingDirectory(PrintWriter out, String currentDirectory);
}
