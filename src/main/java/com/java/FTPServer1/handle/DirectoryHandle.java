package com.java.FTPServer1.handle;

import java.io.PrintWriter;

public interface DirectoryHandle {
    void createDirectory(String directoryName, PrintWriter out);
    void removeDirectory(String directoryName, PrintWriter out);
    void changeWorkingDirectory(String directoryName, PrintWriter out);
    void printWorkingDirectory(PrintWriter out);
}
