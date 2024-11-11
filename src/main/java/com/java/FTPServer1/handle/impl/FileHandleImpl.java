package com.java.FTPServer1.handle.impl;

import com.java.FTPServer1.enums.ResponseCode;
import com.java.FTPServer1.enums.TransferType;
import com.java.FTPServer1.handle.FileHandle;
import com.java.FTPServer1.system.UserSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileHandleImpl implements FileHandle {
    private final ConnectionHandleImpl connectionHandle;

    @Override
    public void uploadFile(String fileName, PrintWriter out, UserSession userSession) {
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
                } else if (userSession.getTransferMode() == TransferType.ASCII) {
                    storeFileForASCIIMode(f, out);
                } else {
                    out.println(ResponseCode.NOT_SUPPORTED.getResponse("Unsupported transfer mode"));
                }
            }
        }
        log.info("Completed receiving file {} ", f.getName());
        out.println(ResponseCode.FILE_COMPLETED_TRANSFER.getResponse());
        connectionHandle.closeDataConnection();
    }

    @Override
    public void downloadFile(String fileName, PrintWriter out) {

    }

    @Override
    public void appendToFile(String fileName, PrintWriter out) {

    }

    @Override
    public void deleteFile(String fileName, PrintWriter out) {

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
        out.println(ResponseCode.FILE_STARTING_TRANSFER.getResponse("Opening binary mode data connection for requested file"));
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
            connectionHandle.toString();
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
}
