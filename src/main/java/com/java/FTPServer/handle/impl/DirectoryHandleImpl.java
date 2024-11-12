package com.java.FTPServer.handle.impl;

import com.java.FTPServer.enums.ResponseCode;
import com.java.FTPServer.handle.DirectoryHandle;
import com.java.FTPServer.system.ConstFTP;
import com.java.FTPServer.system.UserSession;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.PrintWriter;
import java.util.Objects;

@Component
public class DirectoryHandleImpl implements DirectoryHandle {
    @Override
    public void createDirectory(String directoryName, PrintWriter out, String currDirectory) {
        try {
            File directory = new File(currDirectory + "/" + directoryName);
            if (!directory.exists()) {
                if (directory.mkdirs()) {
                    out.println(ResponseCode.OPERATION_OK.getResponse("Directory created: " + directoryName));
                } else {
                    out.println(ResponseCode.FILE_CONFLICT.getResponse("Create directory operation failed"));
                }
            } else {
                out.println(ResponseCode.FILE_CONFLICT.getResponse("Create directory operation failed"));
            }
        } catch (Exception e) {
            out.println(ResponseCode.FILE_CONFLICT.getResponse("Create directory operation failed"));
        }
    }

    @Override
    public void removeDirectory(String directoryName, PrintWriter out, String currDirectory) {
        try {
            File directory = new File(currDirectory + "/" + directoryName);
            if (directory.exists() && directory.isDirectory()) {
                if (Objects.requireNonNull(directory.list()).length == 0) {
                    directory.delete();
                    out.println(ResponseCode.OPERATION_OK.getResponse("Directory removed: " + directoryName));
                } else {
                    out.println(ResponseCode.FILE_CONFLICT.getResponse("Remove directory operation failed"));
                }
            } else {
                out.println(ResponseCode.FILE_CONFLICT.getResponse("Remove directory operation failed"));
            }
        } catch (Exception e) {
            out.println(ResponseCode.FILE_CONFLICT.getResponse("Remove directory operation failed"));
        }
    }

    @Override
    public void changeWorkingDirectory(String directoryName, PrintWriter out, UserSession userSession) {
        try {
            String newWorkingDirectory = userSession.getCurrDirectory() + "\\" + directoryName;
            File newDirectory = new File(newWorkingDirectory);
            if (newDirectory.exists() && newDirectory.isDirectory()) {
                userSession.setCurrDirectory(newWorkingDirectory);
                out.println(ResponseCode.OPERATION_OK.getResponse("Changed working directory to: " + directoryName));
            } else {
                out.println(ResponseCode.FILE_CONFLICT.getResponse("Change directory operation failed: Directory does not exist"));
            }
        } catch (Exception e) {
            out.println(ResponseCode.FILE_CONFLICT.getResponse("Change directory operation failed due to an error"));
        }
    }

    @Override
    public void printWorkingDirectory(PrintWriter out, String currentDirectory) {
        File currentDirectoryViewForUser = new File(currentDirectory + "\\");
        String currentDirectoryViewForUserStr = currentDirectoryViewForUser.toString()
                .substring(ConstFTP.ROOT_DIR_FOR_USER.length());
        String response = currentDirectoryViewForUserStr + "\\" + " is a current directory";
        out.println(ResponseCode.OPERATION_OK.getResponse(response));
    }
}
