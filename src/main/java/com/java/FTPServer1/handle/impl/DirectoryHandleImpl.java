package com.java.FTPServer1.handle.impl;

import com.java.FTPServer1.handle.DirectoryHandle;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;

@Component
public class DirectoryHandleImpl implements DirectoryHandle {
    @Override
    public void createDirectory(String directoryName, PrintWriter out) {

    }

    @Override
    public void removeDirectory(String directoryName, PrintWriter out) {

    }

    @Override
    public void changeWorkingDirectory(String directoryName, PrintWriter out) {

    }

    @Override
    public void printWorkingDirectory(PrintWriter out) {

    }
}
