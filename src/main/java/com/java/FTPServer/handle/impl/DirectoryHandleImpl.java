package com.java.FTPServer.handle.impl;

import com.java.FTPServer.enums.ResponseCode;
import com.java.FTPServer.handle.DirectoryHandle;
import com.java.FTPServer.system.ConstFTP;
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
    public void printWorkingDirectory(PrintWriter out, String currentDirectory) {
        String currentDirectoryViewForUser = currentDirectory + "\\";
        currentDirectoryViewForUser = currentDirectoryViewForUser
                .substring(ConstFTP.ROOT_DIR_FOR_USER.length());
        String response = currentDirectoryViewForUser + " is a current directory";
        out.println(ResponseCode.OPERATION_OK.getResponse(response));
    }
}
