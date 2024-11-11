package com.java.FTPServer1.handle;

import com.java.FTPServer1.system.UserSession;

import java.io.PrintWriter;

public interface FileHandle {
    void uploadFile(String fileName, PrintWriter out, UserSession userSession);
    void downloadFile(String fileName, PrintWriter out);
    void appendToFile(String fileName, PrintWriter out);
    void deleteFile(String fileName, PrintWriter out);
}
