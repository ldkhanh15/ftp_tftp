package com.java.FTPServer.handle;

import org.springframework.stereotype.Component;

import java.io.*;
import java.net.Socket;

@Component
public class CommandHandlerImpl implements CommandHandler{
    private static final String ROOT_DIRECTORY = "ftp_root/";
    @Override
    public void upload(Socket controlSocket,
                       String fileName) throws Exception {
        try (FileOutputStream fileOutputStream = new FileOutputStream(ROOT_DIRECTORY + fileName);
        DataOutputStream dataOutputStream = new DataOutputStream(controlSocket.getOutputStream());
        DataInputStream dataInputStream = new DataInputStream(controlSocket.getInputStream())) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = dataInputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }
            dataOutputStream.flush();
            dataOutputStream.writeUTF("226 Transfer complete.");
        }
    }

    @Override
    public void download(Socket controlSocket, String fileName) throws Exception {
        try(DataOutputStream dataOutputStream = new DataOutputStream(controlSocket.getOutputStream());
            DataInputStream dataInputStream = new DataInputStream(controlSocket.getInputStream())){
            File file = new File(ROOT_DIRECTORY + fileName);
            System.out.println("Checking file at: " + file.getAbsolutePath());

            if (file.exists()) {
                dataOutputStream.writeUTF("FILE_FOUND");

                try (FileInputStream fileInputStream = new FileInputStream(file)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;

                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                        dataOutputStream.write(buffer, 0, bytesRead);
                    }
                    dataOutputStream.flush(); // Ensure all data is sent before closing
                    dataOutputStream.writeUTF("226 Transfer complete.");
                }
            } else {
                System.out.println("File not found: " + file.getAbsolutePath());
                dataOutputStream.writeUTF("FILE_NOT_FOUND");
            }
        }
    }


    @Override
    public void logout(DataInputStream dataInputStream, DataOutputStream dataOutputStream, Socket controlSocket) throws Exception {
        dataOutputStream.writeUTF("221 Goodbye.");
        controlSocket.close();
    }
}
