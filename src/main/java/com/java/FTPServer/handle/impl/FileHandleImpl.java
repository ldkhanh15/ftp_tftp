package com.java.FTPServer.handle.impl;

import com.java.FTPServer.enums.ResponseCode;
import com.java.FTPServer.enums.TransferType;
import com.java.FTPServer.handle.FileHandle;
import com.java.FTPServer.system.UserSession;
import com.java.controller.FileController;
import com.java.controller.FolderController;
import com.java.model.Folder;
import com.java.service.FileService;
import com.java.service.FolderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileHandleImpl implements FileHandle {
    private final ConnectionHandleImpl connectionHandle;
    private final FileController fileController;
    private final FolderController folderController;
    private UserSession userSession;
    @Override
    public void uploadFile(PrintWriter out,String fileName,  UserSession userSession) {
        this.userSession=userSession;
        File f = null;
        if (fileName == null) {
            out.println(ResponseCode.NOT_SUPPORTED.getResponse("No filename given"));
        } else {
            f = new File(userSession.getCurrDirectory() + "/" + fileName);
            if (f.exists()) {
                out.println(ResponseCode.FILE_CONFLICT.getResponse("File already exists"));
            } else {
                if (userSession.getTransferMode() == TransferType.BINARY) {
                    storeFileForBinaryMode(f, out);
                    saveFile(f);
                } else if (userSession.getTransferMode() == TransferType.ASCII) {
                    storeFileForASCIIMode(f, out);
                    saveFile(f);
                } else {
                    out.println(ResponseCode.NOT_SUPPORTED.getResponse("Unsupported transfer mode"));
                }
            }
        }
        log.info("Completed upload file {} ", f.getName());
        out.println(ResponseCode.FILE_COMPLETED_TRANSFER.getResponse());
        connectionHandle.closeDataConnection();
    }
    private void saveFile(File f){
        Optional<Folder> folder=folderController.findFolderIdByPath(userSession.getCurrDirectory());
        if(folder.isPresent()){
            com.java.model.File file=new com.java.model.File(f.getName(),f.getPath(),f.length(),
                    getFileType(f.getName()),folder.get());
            fileController.save(file);
        }
    }
    private void saveFile(com.java.model.File fileDB,File f){
        Optional<Folder> folder=folderController.findFolderIdByPath(userSession.getCurrDirectory());
        if(folder.isPresent() && fileDB !=null){
            fileDB.setFileSize(f.length());
            fileController.save(fileDB);
        }
    }
    private String getFileType(String fileName) {
        int lastIndexOfDot = fileName.lastIndexOf('.');
        if (lastIndexOfDot > 0 && lastIndexOfDot < fileName.length() - 1) {
            return fileName.substring(lastIndexOfDot + 1).toLowerCase();
        }
        return "unknown";
    }

    @Override
    public void downloadFile(PrintWriter out,String fileName,  UserSession userSession) {
        this.userSession=userSession;
        File f = new File(userSession.getCurrDirectory() + "/" + fileName);
        if (!f.exists()) {
            out.println(ResponseCode.FILE_CONFLICT.getResponse("File does not exist"));
        }
        else{
            if (userSession.getTransferMode() == TransferType.BINARY) {
                retrieveFileForBinaryMode(f, out);
            }
            else if (userSession.getTransferMode() == TransferType.ASCII) {
                retrieveFileForASCIIMode(f, out);
            }
            else {
                out.println(ResponseCode.NOT_SUPPORTED.getResponse("Unsupported transfer mode"));
            }
        }
        log.info("Completed download file {} ", f.getName());
        out.println(ResponseCode.FILE_COMPLETED_TRANSFER.getResponse());
        connectionHandle.closeDataConnection();
    }

    @Override
    public void appendToFile( PrintWriter out,String fileName, UserSession userSession) {
        this.userSession=userSession;
        File f = null;
        if (fileName == null) {
            out.println(ResponseCode.NOT_SUPPORTED.getResponse("No filename given"));
        } else {
            f = new File(userSession.getCurrDirectory() + "/" + fileName);
            com.java.model.File file=null;
            Optional<Folder> folder=folderController.findFolderIdByPath(userSession.getCurrDirectory());
            if(folder.isPresent()){
                file=fileController.findByFileNameAndFolderParent(fileName,folder.get());
            }
            if (f.exists()) {
                // lay file ra file de append
                if (userSession.getTransferMode() == TransferType.BINARY) {
                    appendToFileForBinaryMode(f, out);
                    saveFile(file,f);
                } else if (userSession.getTransferMode() == TransferType.ASCII) {
                    appendToFileForASCIIMode(f, out);
                    saveFile(file,f);
                } else {
                    out.println(ResponseCode.NOT_SUPPORTED.getResponse("Unsupported transfer mode"));
                }
            } else {
                if (userSession.getTransferMode() == TransferType.BINARY) {
                    storeFileForBinaryMode(f, out);
                    saveFile(f);
                } else if (userSession.getTransferMode() == TransferType.ASCII) {
                    storeFileForASCIIMode(f, out);
                    saveFile(f);
                } else {
                    out.println(ResponseCode.NOT_SUPPORTED.getResponse("Unsupported transfer mode"));
                }
            }
        }
        log.info("Completed upload file {} ", f.getName());
        out.println(ResponseCode.FILE_COMPLETED_TRANSFER.getResponse());
        connectionHandle.closeDataConnection();
    }

    @Override
    public void deleteFile(PrintWriter out,String fileName,  UserSession userSession) {
        this.userSession=userSession;
        if (fileName == null || fileName.trim().isEmpty()){
            out.println(ResponseCode.NOT_SUPPORTED.getResponse("No filename given"));
            return;
        }
        File file = new File(userSession.getCurrDirectory() + "/" + fileName);
        if (!file.exists()) {
            out.println(ResponseCode.FILE_CONFLICT.getResponse("Delete operation failed"));
            return;
        }
        if (!file.isFile()) {
            out.println(ResponseCode.FILE_CONFLICT.getResponse("Delete operation failed"));
            return;
        }
        if (file.delete()) {
            Optional<Folder> folder=folderController.findFolderIdByPath(userSession.getCurrDirectory());
            if(folder.isPresent()){
                com.java.model.File fileDB=fileController.findByFileNameAndFolderParent(fileName,folder.get());
                fileController.deleteById(fileDB.getItemId());
            }
            out.println(ResponseCode.FILE_COMPLETED_TRANSFER.getResponse("File deleted successfully"));
            log.info("File {} deleted successfully", file.getName());
        } else {
            out.println(ResponseCode.FILE_CONFLICT.getResponse("Delete operation failed"));
            log.error("Failed to delete file {}", file.getName());
        }

    }

    private void storeFileForBinaryMode(File file, PrintWriter out){
        BufferedOutputStream fout = null;
        BufferedInputStream fin = null;
        out.println(ResponseCode.FILE_STARTING_TRANSFER.getResponse("Opening binary mode data connection for requested file"));
        try {
            fout = new BufferedOutputStream(new FileOutputStream(file));
            fin = new BufferedInputStream(connectionHandle.getDataConnection().getInputStream());
        } catch (Exception e) {
            log.error("Could not create byte streams {}", e.getMessage());
        }
        log.info("Start receiving file {} ", file.getName());

        byte[] buf = new byte[1024];
        int l = 0;
        try {
            while ((l = fin.read(buf, 0, 1024)) != -1) {
                if (fout != null) {
                    fout.write(buf, 0, l);
                }
            }
        } catch (IOException e) {
            log.error("Could not read from or write to byte streams {}", e.getMessage());
        }
        try {
            fin.close();
            if (fout != null) {
                fout.close();
            }
        } catch (IOException e) {
            log.error("Could not close byte streams {}", e.getMessage());
        }
    }

    private void storeFileForASCIIMode(File file, PrintWriter out) {
        out.println(ResponseCode.FILE_STARTING_TRANSFER.getResponse("Opening ascii mode data connection for requested file"));
        BufferedReader rin = null;
        PrintWriter rout = null;
        try {
            rin = new BufferedReader(new InputStreamReader(connectionHandle.getDataConnection().getInputStream()));
            rout = new PrintWriter(new FileOutputStream(file), true);
        } catch (IOException e) {
            log.error("Could not create character streams {}", e.getMessage());
            System.err.println(e.getMessage());
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
        }
        try {
            String s = "";
            while ((s = rin.readLine()) != null) {
                if (rout != null) {
                    rout.println(s);
                }
            }
        } catch (IOException e) {
            log.error("Could not read from or write to character streams {}", e.getMessage());
        }
        try {
            if (rout != null) {
                rout.close();
            }
            rin.close();
        } catch (IOException e) {
            log.error("Could not close character streams {}", e.getMessage());
        }
    }

    private void retrieveFileForBinaryMode(File file, PrintWriter out) {
        out.println(ResponseCode.FILE_STARTING_TRANSFER.getResponse("Opening binary mode data connection for requested file"));
        BufferedOutputStream fout = null;
        BufferedInputStream fin = null;

        try {
            fout = new BufferedOutputStream(connectionHandle.getDataConnection().getOutputStream());
            fin = new BufferedInputStream(new FileInputStream(file));
        }
        catch (IOException e) {
            log.error("Could not create byte streams {}", e.getMessage());
            System.err.println(e.getMessage());
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
        }

        byte[] buf = new byte[1024];
        int l = 0;
        try {
            while ((l = fin.read(buf, 0, 1024)) != -1) {
                fout.write(buf, 0, l);
            }
        } catch (IOException e) {
            log.error("Could not read from or write to byte streams {}", e.getMessage());
        }

        try {
            fin.close();
            fout.close();
        } catch (IOException e) {
            log.error("Could not close byte streams {}", e.getMessage());
        }
    }

    private void retrieveFileForASCIIMode(File file, PrintWriter out) {
        out.println(ResponseCode.FILE_STARTING_TRANSFER.getResponse("Opening ascii mode data connection for requested file"));

        BufferedReader rin = null;
        PrintWriter rout = null;

        try {
            rin = new BufferedReader(new FileReader(file));
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

        try {
            while ((s = rin.readLine()) != null) {
                rout.println(s);
            }
        } catch (IOException e) {
            log.error("Could not read from or write to byte streams {}", e.getMessage());
        }

        try {
            rout.close();
            rin.close();
        } catch (IOException e) {
            log.error("Could not close byte streams {}", e.getMessage());
        }
    }

    private void appendToFileForBinaryMode(File file, PrintWriter out) {
        out.println(ResponseCode.FILE_STARTING_TRANSFER.getResponse("Opening binary mode data connection for appending to file"));
        try (BufferedOutputStream fout = new BufferedOutputStream(new FileOutputStream(file, true));
             BufferedInputStream fin = new BufferedInputStream(connectionHandle.getDataConnection().getInputStream())) {

            byte[] buf = new byte[1024];
            int l;
            while ((l = fin.read(buf)) != -1) {
                fout.write(buf, 0, l);
            }
        } catch (IOException e) {
            log.error("Error during binary append to file {}", e.getMessage());
            out.println(ResponseCode.FILE_CONFLICT.getResponse("Error during ASCII append to file"));
        }
    }

    private void appendToFileForASCIIMode(File file, PrintWriter out) {
        out.println(ResponseCode.FILE_STARTING_TRANSFER.getResponse("Opening ASCII mode data connection for appending to file"));
        try (BufferedReader rin = new BufferedReader(new InputStreamReader(connectionHandle.getDataConnection().getInputStream()));
             PrintWriter rout = new PrintWriter(new FileOutputStream(file, true))) {

            String line;
            while ((line = rin.readLine()) != null) {
                rout.println(line);
            }
        } catch (IOException e) {
            log.error("Error during ASCII append to file {}", e.getMessage());
            out.println(ResponseCode.FILE_CONFLICT.getResponse("Error during ASCII append to file"));
        }
    }
}
