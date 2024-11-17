package com.java.FTPServer.handle;

import com.java.FTPServer.system.UserSession;

import java.io.PrintWriter;

public interface DirectoryHandle {
    void createDirectory(PrintWriter out, String directoryName ,String currDirectory);
    void removeDirectory( PrintWriter out,String directoryName, String currDirectory);
    void changeWorkingDirectory( PrintWriter out,String directoryName, UserSession userSession);
    void printWorkingDirectory(PrintWriter out, String currentDirectory);
}
