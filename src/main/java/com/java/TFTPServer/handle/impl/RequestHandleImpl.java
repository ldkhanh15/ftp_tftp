package com.java.TFTPServer.handle.impl;

import com.java.FTPServer.ulti.UserSessionManager;
import com.java.TFTPServer.custom.NetAsciiInputStream;
import com.java.TFTPServer.custom.NetAsciiOutputStream;
import com.java.TFTPServer.enums.Opcode;
import com.java.TFTPServer.enums.ServerResponseErrorCode;
import com.java.TFTPServer.handle.DataAndAckHandle;
import com.java.TFTPServer.handle.ErrorHandle;
import com.java.TFTPServer.handle.RequestHandle;
import com.java.TFTPServer.system.ConstTFTP;
import com.java.controller.FileController;
import com.java.controller.FolderController;
import com.java.model.Folder;
import com.java.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Optional;


@Component
public class RequestHandleImpl implements RequestHandle {
    private final DataAndAckHandle dataAndAckHandle;
    private final ErrorHandle errorHandle;
    private final FileController fileController;
    private final FolderController folderController;

    @Autowired
    public RequestHandleImpl(DataAndAckHandle dataAndAckHandle, ErrorHandle errorHandle, FileController fileController, FolderController folderController) {
        this.dataAndAckHandle = dataAndAckHandle;
        this.errorHandle = errorHandle;
        this.fileController = fileController;
        this.folderController = folderController;
    }
    public static String mode;
    @Override
    public InetSocketAddress receiveFrom(DatagramSocket socket, byte[] buf) {
        DatagramPacket receivePacket = new DatagramPacket(buf, buf.length);

        try {
            socket.receive(receivePacket);
        } catch (IOException e) {
            e.printStackTrace();
        }

        InetSocketAddress client = new InetSocketAddress(receivePacket.getAddress(), receivePacket.getPort());

        return client;
    }

    @Override
    public short ParseRQ(byte[] buf, StringBuffer requestedFile) {
        ByteBuffer wrap = ByteBuffer.wrap(buf);
        short opcode = wrap.getShort();
        int delimiter = -1;
        for (int i = 2; i < buf.length; i++) {
            if (buf[i] == 0) {
                delimiter = i;
                break;
            }
        }

        if (delimiter == -1) {
            System.err.println("Corrupt request packet. Shutting down I guess.");
//            System.exit(1);
        }

        String fileName = new String(buf, 2, delimiter - 2);
        requestedFile.append(fileName);

        for (int i = delimiter + 1; i < buf.length; i++) {
            if (buf[i] == 0) {
                String temp = new String(buf, delimiter + 1, i - (delimiter + 1));
                mode = temp;
                if (temp.equalsIgnoreCase(ConstTFTP.MODE_OCTET) || temp.equalsIgnoreCase(ConstTFTP.MODE_NETASCII)) {
                    return opcode;
                } else {
                    System.err.println("No mode specified.");
                    //System.exit(1);
                }
            }
        }
        System.err.println("Did not find delimiter.");
        //System.exit(1);
        return 0;
    }

    @Override
    public void HandleRQ(DatagramSocket sendSocket, String fileName, int reqType) {
        File file = new File(ConstTFTP.READ_ROOT+"/"+fileName);
        byte[] buf = new byte[ConstTFTP.BUFFER_SIZE - 4];

        if (reqType == Opcode.OP_RRQ.getCode()) {
            try (InputStream in = mode.equalsIgnoreCase(ConstTFTP.MODE_NETASCII) ? new NetAsciiInputStream(new FileInputStream(file)) : new FileInputStream(file)) {
                short blockNum = 1;
                while (true) {
                    int length = in.read(buf);
                    if (length == -1) length = 0;
                    // đóng gói data để gửi cho client
                    DatagramPacket sender = dataAndAckHandle.dataPacket(blockNum, buf, length);
                    System.out.println("Sending.........");
                    // gửi data đi và nhận về ack từ client, kiểm tra đúng ack đúng thì return true, ngược lại thì false
                    if (dataAndAckHandle.SendDataAndReceiveAck(sendSocket, sender, blockNum++)) {
                        System.out.println("Success. Send another. BlockNum = " + blockNum);
                    } else {
                        System.err.println("Error. Lost connection.");
                        errorHandle.sendError(sendSocket, ServerResponseErrorCode.ERR_LOST.getCode(), ServerResponseErrorCode.ERR_LOST.getDescription());
                        return;
                    }
                    if (length < ConstTFTP.BUFFER_SIZE - 4) {
                        in.close();
                        System.out.println("SEND ALL SUCCESSFULLY");
                        break;
                    }
                }
                handleSave(file);
            } catch (FileNotFoundException e) {
                System.err.println("File not found. Sending error packet.");
                errorHandle.sendError(sendSocket, ServerResponseErrorCode.ERR_FNF.getCode(), ServerResponseErrorCode.ERR_FNF.getDescription());
            } catch (IOException e) {
                System.err.println("Error reading file.");
            }
        } else if (reqType == Opcode.OP_WRQ.getCode()) {
            if (file.exists()) {
                System.out.println("File already exists.");
                errorHandle.sendError(sendSocket, ServerResponseErrorCode.ERR_EXISTS.getCode(), ServerResponseErrorCode.ERR_EXISTS.getDescription());
                return;
            }
            try (OutputStream out = mode.equalsIgnoreCase(ConstTFTP.MODE_NETASCII) ? new NetAsciiOutputStream(new FileOutputStream(file)) : new FileOutputStream(file)) {
                short blockNum = 0;
                while (true) {
                    // response ACK cho client, sau đó nhận gói data mới đồng thời kiểm tra có đúng blockNum mong đợi hay không
                    // trả về gói data mới nhận nếu kiểm tra đúng blockNum
                    DatagramPacket dataPacket = dataAndAckHandle.ResponseACKAndReceiveData(sendSocket, dataAndAckHandle.ackPacket(blockNum++), blockNum);
                    if (dataPacket == null) {
                        System.err.println("Error. Lost connection.");
                        errorHandle.sendError(sendSocket, ServerResponseErrorCode.ERR_LOST.getCode(), ServerResponseErrorCode.ERR_LOST.getDescription());
                        out.close();
                        System.out.println("Deleting incomplete file.");
                        file.delete();
                        break;
                    } else {
                        // ghi data vào file
                        byte[] data = dataPacket.getData();
                        out.write(data, 4, dataPacket.getLength() - 4);
                        System.out.println(dataPacket.getLength());
                        if (dataPacket.getLength() - 4 < ConstTFTP.BUFFER_SIZE - 4) {
                            sendSocket.send(dataAndAckHandle.ackPacket(blockNum));
                            System.out.println("All done writing file.");
                            out.close();
                            break;
                        }
                    }
                }
                handleSave(file);
            } catch (IOException e) {
                System.err.println("Error writing file.");
                errorHandle.sendError(sendSocket, ServerResponseErrorCode.ERR_ACCESS.getCode(), ServerResponseErrorCode.ERR_ACCESS.getDescription());
            }
        } else {
            System.err.println("Unknown request type.");
        }
    }
    private void handleSave(File file){
        Optional<Folder> folder=folderController.findFolderIdByPath(ConstTFTP.READ_ROOT);
        if(folder.isPresent()){
            com.java.model.File fileDB=new com.java.model.File(file.getName(),file.getPath(),file.length(),
                    getFileType(file.getName()));
            fileDB.setParentFolder(folder.get());
            fileController.save(fileDB);
        }else{
              Folder parent=folderController.save("ftp_root","public");
            com.java.model.File fileDB=new com.java.model.File(file.getName(),file.getPath(),file.length(),
                    getFileType(file.getName()));
            fileDB.setParentFolder(parent);
            fileDB.setIsPublic(true);
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
}

