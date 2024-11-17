package com.java.FTPServer.handle.impl;

import com.java.FTPServer.anotation.FolderOwnerShip;
import com.java.FTPServer.anotation.ItemOwnerShip;
import com.java.FTPServer.enums.ResponseCode;
import com.java.FTPServer.handle.CommonHandle;
import com.java.enums.AccessType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommonImpl implements CommonHandle {
    private final ConnectionHandleImpl connectionHandle;
    @Override
    @FolderOwnerShip(action = AccessType.READ)
    public void listName(PrintWriter out, String currentDirectory) {
        File directory = new File(currentDirectory);

        if (directory.exists() && directory.isDirectory()) {
            retrieveFileName(out, directory);
        } else {
            out.println("Directory does not exist or is not a directory.");
        }
        connectionHandle.closeDataConnection();
        out.println("226 Directory Send OK");
    }

    @Override
    @FolderOwnerShip(action = AccessType.READ)
    public void listDetail(PrintWriter out, String currentDirectory) {
        File directory = new File(currentDirectory);

        if (directory.exists() && directory.isDirectory()) {
            retrieveFileDetail(out, directory);
        } else {
            out.println("Directory does not exist or is not a directory.");
        }
        connectionHandle.closeDataConnection();
        out.println("226 Directory Send OK");
    }

    @Override
    public void initiateRename( PrintWriter out,String nameOnServer) {

    }

    @Override
    public void finalizeRename( PrintWriter out,String newName) {

    }

    private void retrieveFileName(PrintWriter out, File directory) {
        out.println(ResponseCode.FILE_STARTING_TRANSFER.getResponse("Here comes the directory listing"));
        PrintWriter rout = null;

        try {
            rout = new PrintWriter(connectionHandle.getDataConnection().getOutputStream(), true);

        }
        catch (IOException e) {
            log.error("Could not create byte streams {}", e.getMessage());
            System.err.println(e.getMessage());
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
        }

        String s;

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                rout.println(file.getName());
            }
        } else {
            out.println("No files found in the directory.");
        }
        if (rout != null) {
            rout.close();
        }
    }

    private void retrieveFileDetail(PrintWriter out, File directory) {
        out.println(ResponseCode.FILE_STARTING_TRANSFER.getResponse("Here comes the directory listing"));
        PrintWriter rout = null;

        try {
            rout = new PrintWriter(connectionHandle.getDataConnection().getOutputStream(), true);

        }
        catch (IOException e) {
            log.error("Could not create byte streams {}", e.getMessage());
            System.err.println(e.getMessage());
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
        }

        String s;

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                rout.println(file.getName() + " ...more...");
            }
        } else {
            out.println("No files found in the directory.");
        }
        if (rout != null) {
            rout.close();
        }
    }
}
