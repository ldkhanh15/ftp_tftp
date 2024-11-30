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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommonImpl implements CommonHandle {
    private final ConnectionHandleImpl connectionHandle;
    private Map<String, String> renameCache = new HashMap<>();
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
    public void listDetail(PrintWriter out, String path){
        File directory = new File(path);

        if (directory.exists() && directory.isDirectory()) {
            retrieveFileDetail(out, directory);
        } else {
            out.println("Directory does not exist or is not a directory.");
        }
        connectionHandle.closeDataConnection();
        out.println("226 Directory Send OK");
    }


    @Override
    @FolderOwnerShip(action = AccessType.WRITE)
    public void initiateRename(PrintWriter out, String currentDirectory, String nameOnServer) {
        File oldFile = new File(currentDirectory, nameOnServer);

        if (oldFile.exists()) {
            renameCache.put(currentDirectory, nameOnServer);

            out.println("350 Requested file action pending further information.");
            out.flush();
            log.info("RNFR: File '{}' found. Waiting for RNTO command.", nameOnServer);
        } else {
            out.println("550 Requested action not taken. File not found. 1");
            out.flush();
            log.error("RNFR: File '{}' not found in directory '{}'.", nameOnServer, currentDirectory);
        }
    }

    @Override
    @FolderOwnerShip(action = AccessType.WRITE)
    public void finalizeRename(PrintWriter out, String currentDirectory, String newName) {
        String oldName = renameCache.get(currentDirectory);

        if (oldName != null) {
            File oldFile = new File(currentDirectory, oldName);

            if (oldFile.exists()) {
                File newFile = new File(currentDirectory, newName);

                if (oldFile.renameTo(newFile)) {
                    out.println("250 Requested file action okay, completed.");
                    out.flush();
                    log.info("RNTO: Successfully renamed '{}' to '{}'.", oldName, newName);
                } else {
                    out.println("550 Requested action not taken. Failed to rename file.");
                    out.flush();
                    log.error("RNTO: Failed to rename '{}' to '{}'.", oldName, newName);
                }
            } else {
                out.println("550 Requested action not taken. File not found. 2");
                out.flush();
                log.error("RNTO: File '{}' not found in directory '{}'.", oldName, currentDirectory);
            }
        } else {
            out.println("503 Bad sequence of commands.");
            out.flush();
            log.error("RNTO: No previous RNFR command found for directory '{}'.", currentDirectory);
        }
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



        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                String s="";
                if(file.isDirectory()){
                    s+="d\t";
                    s+="-\t";
                }else{
                    s+="-\t";
                    s+=file.length()+"\t";
                }
                s+=new Date(file.lastModified())+"\t";
                s+=file.getName()+"\n";
                rout.println(s);
            }
        } else {
            out.println("No files found in the directory.");
        }
        if (rout != null) {
            rout.close();
        }
    }
}
