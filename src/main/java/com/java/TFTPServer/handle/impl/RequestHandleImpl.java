package com.java.TFTPServer.handle.impl;

import com.java.FTPServer.ulti.UserSessionManager;
import com.java.TFTPServer.custom.NetAsciiInputStream;
import com.java.TFTPServer.custom.NetAsciiOutputStream;
import com.java.TFTPServer.custom.OpcodeSizeCustom;
import com.java.TFTPServer.enums.Opcode;
import com.java.TFTPServer.enums.ServerResponseErrorCode;
import com.java.TFTPServer.handle.DataAndAckHandle;
import com.java.TFTPServer.handle.ErrorHandle;
import com.java.TFTPServer.handle.RequestHandle;
import com.java.TFTPServer.system.ConstTFTP;
import com.java.controller.FileController;
import com.java.controller.FolderController;
import com.java.controller.UserController;
import com.java.model.Folder;
import com.java.model.User;
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
    private final UserController userController;

    @Autowired
    public RequestHandleImpl(DataAndAckHandle dataAndAckHandle, ErrorHandle errorHandle, FileController fileController, FolderController folderController, UserController userController) {
        this.dataAndAckHandle = dataAndAckHandle;
        this.errorHandle = errorHandle;
        this.fileController = fileController;
        this.folderController = folderController;
        this.userController = userController;
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
//    public short ParseRQ(byte[] buf, StringBuffer requestedFile) {
//        ByteBuffer wrap = ByteBuffer.wrap(buf);
//        short opcode = wrap.getShort();
//        int delimiter = -1;
//        for (int i = 2; i < buf.length; i++) {
//            if (buf[i] == 0) {
//                delimiter = i;
//                break;
//            }
//        }
//
//        if (delimiter == -1) {
//            System.err.println("Corrupt request packet. Shutting down I guess.");
////            System.exit(1);
//        }
//
//        String fileName = new String(buf, 2, delimiter - 2);
//        requestedFile.append(fileName);
//
//        for (int i = delimiter + 1; i < buf.length; i++) {
//            if (buf[i] == 0) {
//                String temp = new String(buf, delimiter + 1, i - (delimiter + 1));
//                mode = temp;
//                if (temp.equalsIgnoreCase(ConstTFTP.MODE_OCTET) || temp.equalsIgnoreCase(ConstTFTP.MODE_NETASCII)) {
//                    return opcode;
//                } else {
//                    System.err.println("No mode specified.");
//                    //System.exit(1);
//                }
//            }
//        }
//        System.err.println("Did not find delimiter.");
//        //System.exit(1);
//        return 0;
//    }

    public OpcodeSizeCustom ParseRQ(byte[] buf, StringBuffer requestedFile) {
        ByteBuffer wrap = ByteBuffer.wrap(buf);
        short opcode = wrap.getShort();  // Lấy opcode từ gói tin
        int delimiter = -1;
        int blksize = 0;  // Mặc định giá trị blksize là 0

        // Tìm delimiter (null byte) để tách tên file và mode
        for (int i = 2; i < buf.length; i++) {
            if (buf[i] == 0) {
                delimiter = i;
                break;
            }
        }

        if (delimiter == -1) {
            System.err.println("Corrupt request packet.");
            return null;  // Nếu gói tin bị lỗi, trả về null
        }

        // Lấy tên file
        String fileName = new String(buf, 2, delimiter - 2);
        requestedFile.setLength(0);
        requestedFile.append(fileName);  // Append tên file vào StringBuffer

        // Tìm mode và kiểm tra xem có blksize không
//        String mode = null;
        for (int i = delimiter + 1; i < buf.length; i++) {
            if (buf[i] == 0) {
                String temp = new String(buf, delimiter + 1, i - (delimiter + 1));
                mode = temp;
                if (temp.equalsIgnoreCase(ConstTFTP.MODE_NETASCII) || temp.equalsIgnoreCase(ConstTFTP.MODE_OCTET)) {
                    // Kiểm tra tùy chọn blksize nếu có
                    int optionStart = i + 1;
                    if (optionStart < buf.length) {  // Kiểm tra còn dữ liệu sau delimiter không
                        String option = new String(buf, optionStart, 7);  // Tìm "blksize"
                        if ("blksize".equals(option)) {
                            int sizeStart = optionStart + 8;  // Sau "blksize\0"
                            int sizeEnd = -1;
                            for (int j = sizeStart; j < buf.length; j++) {
                                if (buf[j] == 0) {
                                    sizeEnd = j;
                                    break;
                                }
                            }
                            if (sizeEnd > sizeStart) {
                                String blkSizeStr = new String(buf, sizeStart, sizeEnd - sizeStart);
                                blksize = Integer.parseInt(blkSizeStr);
                            }
                        }
                    }
                    if (blksize == 0)
                        blksize = 516;  // Nếu không tìm thấy blksize, mặc định là 516
                    return new OpcodeSizeCustom(opcode, blksize);  // Trả về đối tượng với opcode và blksize
                } else {
                    System.err.println("Invalid mode specified.");
                    return null;
                }
            }
        }

        return null;  // Nếu không tìm thấy delimiter hoặc mode hợp lệ, trả về null
    }



    @Override
    public void HandleRQ(DatagramSocket sendSocket, String fileName, int reqType, int SIZE) {
        File file = new File(ConstTFTP.READ_ROOT+"/"+fileName);
        byte[] buf = new byte[SIZE - 4];

        if (reqType == Opcode.OP_RRQ.getCode()) {
            try (InputStream in = mode.equalsIgnoreCase(ConstTFTP.MODE_NETASCII) ? new NetAsciiInputStream(new FileInputStream(file)) : new FileInputStream(file)) {
                short blockNum = 1;
                while (true) {
                    int length = in.read(buf);
                    if (length == -1) length = 0;
                    // đóng gói data để gửi cho client
                    DatagramPacket sender = dataAndAckHandle.dataPacket(blockNum, buf, length, SIZE);
                    System.out.println("Sending.........");
                    // gửi data đi và nhận về ack từ client, kiểm tra đúng ack đúng thì return true, ngược lại thì false
                    if (dataAndAckHandle.SendDataAndReceiveAck(sendSocket, sender, blockNum++, SIZE)) {
                        System.out.println("Success. Send another. BlockNum = " + blockNum);
                    } else {
                        System.err.println("Error. Lost connection.");
                        errorHandle.sendError(sendSocket, ServerResponseErrorCode.ERR_LOST.getCode(), ServerResponseErrorCode.ERR_LOST.getDescription(), SIZE);
                        return;
                    }
                    if (length < SIZE - 4) {
                        in.close();
                        System.out.println("SEND ALL SUCCESSFULLY");
                        break;
                    }
                }

            } catch (FileNotFoundException e) {
                System.err.println("File not found. Sending error packet.");
                errorHandle.sendError(sendSocket, ServerResponseErrorCode.ERR_FNF.getCode(), ServerResponseErrorCode.ERR_FNF.getDescription(), SIZE);
            } catch (IOException e) {
                System.err.println("Error reading file.");
            }
        } else if (reqType == Opcode.OP_WRQ.getCode()) {
            if (file.exists()) {
                System.out.println("File already exists.");
                errorHandle.sendError(sendSocket, ServerResponseErrorCode.ERR_EXISTS.getCode(), ServerResponseErrorCode.ERR_EXISTS.getDescription(), SIZE);
                return;
            }
            try (OutputStream out = mode.equalsIgnoreCase(ConstTFTP.MODE_NETASCII) ? new NetAsciiOutputStream(new FileOutputStream(file)) : new FileOutputStream(file)) {
                short blockNum = 0;
                while (true) {
                    // response ACK cho client, sau đó nhận gói data mới đồng thời kiểm tra có đúng blockNum mong đợi hay không
                    // trả về gói data mới nhận nếu kiểm tra đúng blockNum
                    DatagramPacket dataPacket = dataAndAckHandle.ResponseACKAndReceiveData(sendSocket, dataAndAckHandle.ackPacket(blockNum++, SIZE), blockNum, SIZE);
                    if (dataPacket == null) {
                        System.err.println("Error. Lost connection.");
                        errorHandle.sendError(sendSocket, ServerResponseErrorCode.ERR_LOST.getCode(), ServerResponseErrorCode.ERR_LOST.getDescription(), SIZE);
                        out.close();
                        System.out.println("Deleting incomplete file.");
                        file.delete();
                        break;
                    } else {
                        // ghi data vào file
                        byte[] data = dataPacket.getData();
                        out.write(data, 4, dataPacket.getLength() - 4);
                        System.out.println(dataPacket.getLength());
                        if (dataPacket.getLength() - 4 < SIZE - 4) {
                            sendSocket.send(dataAndAckHandle.ackPacket(blockNum, SIZE));
                            System.out.println("All done writing file.");
                            out.close();
                            handleSave(file);
                            break;
                        }
                    }
                }

            } catch (IOException e) {
                System.err.println("Error writing file.");
                errorHandle.sendError(sendSocket, ServerResponseErrorCode.ERR_ACCESS.getCode(), ServerResponseErrorCode.ERR_ACCESS.getDescription(), SIZE);
            }
        } else {
            System.err.println("Unknown request type.");
        }
    }


    private void handleSave(File file) {
        Optional<Folder> folder = folderController.findFolderIdByPath(ConstTFTP.READ_ROOT);
        if (folder.isPresent()) {
            Optional<com.java.model.File> fileDB=fileController.findByPath(file.getPath());
            com.java.model.File fileSave=null;
            if(fileDB.isPresent()){
                fileSave=fileDB.get();
                fileSave.setFileSize(file.length());
            }else{
                fileSave = new com.java.model.File(file.getName(), file.getPath(), file.length(),
                        getFileType(file.getName()));
                fileSave.setParentFolder(folder.get());
                User user = userController.findByUsername("anonymous");
                fileSave.setOwner(user);
                fileSave.setIsPublic(true);
            }
            fileController.save(fileSave);
        } else {
            Folder parent = folderController.save("ftp_root", "public", "anonymous");
            com.java.model.File fileDB = new com.java.model.File(file.getName(), file.getPath(), file.length(),
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

