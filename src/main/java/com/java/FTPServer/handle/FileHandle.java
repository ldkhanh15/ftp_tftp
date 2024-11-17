package com.java.FTPServer.handle;

import com.java.FTPServer.system.UserSession;

import java.io.PrintWriter;

public interface FileHandle {
    void uploadFile(PrintWriter out, String fileName, UserSession userSession);
    void downloadFile(PrintWriter out, String fileName, UserSession userSession);
    void appendToFile(PrintWriter out, String fileName, UserSession userSession);
    void deleteFile(PrintWriter out, String fileName, UserSession userSession);

}
