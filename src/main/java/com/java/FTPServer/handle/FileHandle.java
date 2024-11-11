package com.java.FTPServer.handle;

import com.java.FTPServer.system.UserSession;

import java.io.PrintWriter;

public interface FileHandle {
    void uploadFile(String fileName, PrintWriter out, UserSession userSession);
    void downloadFile(String fileName, PrintWriter out, UserSession userSession);
    void appendToFile(String fileName, PrintWriter out, UserSession userSession);
    void deleteFile(String fileName, PrintWriter out);
}
