package com.java.TFTPServer.system;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;

import com.java.TFTPServer.custom.NetAsciiInputStream;
import com.java.TFTPServer.custom.NetAsciiOutputStream;
import com.java.TFTPServer.enums.ClientResponseErrorCode;
import com.java.TFTPServer.enums.Opcode;
import com.java.TFTPServer.enums.ServerResponseErrorCode;
import com.java.TFTPServer.handle.DataAndAckHandle;
import com.java.TFTPServer.handle.ErrorHandle;
import com.java.TFTPServer.handle.RequestHandle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TFTPServer {

    public static String mode;
    private final RequestHandle requestHandle;

    @Autowired
    public TFTPServer(RequestHandle requestHandle) {
        this.requestHandle = requestHandle;

        File readDir = new File(ConstTFTP.READ_ROOT);
        if (!readDir.exists()) {
            readDir.mkdirs();
        }

        File writeDir = new File(ConstTFTP.WRITE_ROOT);
        if (!writeDir.exists()) {
            writeDir.mkdirs();
        }
    }


    public void start() throws SocketException {
        byte[] buf = new byte[ConstTFTP.BUFFER_SIZE];

        DatagramSocket socket = new DatagramSocket(null);

        SocketAddress localBindPoint = new InetSocketAddress(ConstTFTP.PORT_TFTP);
        socket.bind(localBindPoint);

        System.out.println("Listening at port " + ConstTFTP.PORT_TFTP + " for new requests");

        while (true) {
            final InetSocketAddress clientAddress = requestHandle.receiveFrom(socket, buf);
            if (clientAddress == null)
                continue;

            final StringBuffer requestedFile = new StringBuffer();
            final int reqtype = requestHandle.ParseRQ(buf, requestedFile);

            new Thread() {
                public void run() {
                    try {
                        DatagramSocket sendSocket = new DatagramSocket(0);
                        sendSocket.connect(clientAddress);

                        String requestFromClient = (reqtype == Opcode.OP_RRQ.getCode()) ? "Read" : "Write";
                        System.out.println(requestFromClient + " request for " + requestedFile.toString() +
                                " from " + clientAddress.getHostName() + " using port " + clientAddress.getPort());

                        if (reqtype == Opcode.OP_RRQ.getCode()) {
                            requestedFile.insert(0, ConstTFTP.READ_ROOT);
                            requestHandle.HandleRQ(sendSocket, requestedFile.toString(), Opcode.OP_RRQ.getCode());
                        } else {
                            requestedFile.insert(0, ConstTFTP.WRITE_ROOT);
                            requestHandle.HandleRQ(sendSocket, requestedFile.toString(), Opcode.OP_WRQ.getCode());
                        }
                        sendSocket.close();
                    } catch (SocketException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    }

//    private InetSocketAddress receiveFrom(DatagramSocket socket, byte[] buf) {
//        DatagramPacket receivePacket = new DatagramPacket(buf, buf.length);
//
//        try {
//            socket.receive(receivePacket);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        InetSocketAddress client = new InetSocketAddress(receivePacket.getAddress(), receivePacket.getPort());
//
//        return client;
//    }
//
//    private short ParseRQ(byte[] buf, StringBuffer requestedFile) {
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
//            System.exit(1);
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
//                    System.exit(1);
//                }
//            }
//        }
//        System.err.println("Did not find delimiter.");
//        System.exit(1);
//        return 0;
//    }
//
//    private void HandleRQ(DatagramSocket sendSocket, String fileName, int reqType) {
//        File file = new File(fileName);
//        byte[] buf = new byte[ConstTFTP.BUFFER_SIZE - 4];
//
//        if (reqType == Opcode.OP_RRQ.getCode()) {
//            try (InputStream in = mode.equalsIgnoreCase(ConstTFTP.MODE_NETASCII) ? new NetAsciiInputStream(new FileInputStream(file)) : new FileInputStream(file)) {
//                short blockNum = 1;
//                while (true) {
//                    int length = in.read(buf);
//                    if (length == -1) length = 0;
//                    // đóng gói data để gửi cho client
//                    DatagramPacket sender = dataPacket(blockNum, buf, length);
//                    System.out.println("Sending.........");
//                    // gửi data đi và nhận về ack từ client, kiểm tra đúng ack đúng thì return true, ngược lại thì false
//                    if (SendDataAndReceiveAck(sendSocket, sender, blockNum++)) {
//                        System.out.println("Success. Send another. BlockNum = " + blockNum);
//                    } else {
//                        System.err.println("Error. Lost connection.");
//                        sendError(sendSocket, ServerResponseErrorCode.ERR_LOST.getCode(), ServerResponseErrorCode.ERR_LOST.getDescription());
//                        return;
//                    }
//                    if (length < 512) {
//                        in.close();
//                        break;
//                    }
//                }
//            } catch (FileNotFoundException e) {
//                System.err.println("File not found. Sending error packet.");
//                sendError(sendSocket, ServerResponseErrorCode.ERR_FNF.getCode(), ServerResponseErrorCode.ERR_FNF.getDescription());
//            } catch (IOException e) {
//                System.err.println("Error reading file.");
//            }
//        } else if (reqType == Opcode.OP_WRQ.getCode()) {
//            if (file.exists()) {
//                System.out.println("File already exists.");
//                sendError(sendSocket, ServerResponseErrorCode.ERR_EXISTS.getCode(), ServerResponseErrorCode.ERR_EXISTS.getDescription());
//                return;
//            }
//            try (OutputStream out = mode.equalsIgnoreCase(ConstTFTP.MODE_NETASCII) ? new NetAsciiOutputStream(new FileOutputStream(file)) : new FileOutputStream(file)) {
//                short blockNum = 0;
//                while (true) {
//                    // response ACK cho client, sau đó nhận gói data mới đồng thời kiểm tra có đúng blockNum mong đợi hay không
//                    // trả về gói data mới nhận nếu kiểm tra đúng blockNum
//                    DatagramPacket dataPacket = ResponseACKAndReceiveData(sendSocket, ackPacket(blockNum++), blockNum);
//                    if (dataPacket == null) {
//                        System.err.println("Error. Lost connection.");
//                        sendError(sendSocket, ServerResponseErrorCode.ERR_LOST.getCode(), ServerResponseErrorCode.ERR_LOST.getDescription());
//                        out.close();
//                        System.out.println("Deleting incomplete file.");
//                        file.delete();
//                        break;
//                    } else {
//                        // ghi data vào file
//                        byte[] data = dataPacket.getData();
//                        out.write(data, 4, dataPacket.getLength() - 4);
//                        System.out.println(dataPacket.getLength());
//                        if (dataPacket.getLength() - 4 < 512) {
//                            sendSocket.send(ackPacket(blockNum));
//                            System.out.println("All done writing file.");
//                            out.close();
//                            break;
//                        }
//                    }
//                }
//            } catch (IOException e) {
//                System.err.println("Error writing file.");
//                sendError(sendSocket, ServerResponseErrorCode.ERR_ACCESS.getCode(), ServerResponseErrorCode.ERR_ACCESS.getDescription());
//            }
//        } else {
//            System.err.println("Unknown request type.");
//        }
//    }
//
//
//
//
//    private DatagramPacket ResponseACKAndReceiveData(DatagramSocket sendSocket, DatagramPacket sendAck, short block) {
//        int retryCount = 0;
//        byte[] rec = new byte[ConstTFTP.BUFFER_SIZE];
//        DatagramPacket receiver = new DatagramPacket(rec, rec.length);
//
//        while (true) {
//            if (retryCount >= 6) {
//                System.err.println("Timed out. Closing connection.");
//                return null;
//            }
//            try {
//                System.out.println("sending ack for block: " + block);
//                sendSocket.send(sendAck);
//                sendSocket.setSoTimeout(((int) Math.pow(2, retryCount++)) * 1000);
//                sendSocket.receive(receiver);
//
//                short blockNum = getData(receiver);
//                System.out.println("Block received: " + blockNum + ", Block expected: " + block);
//                if (blockNum == block) {
//                    return receiver;
//                } else if (blockNum == -1) {
//                    return null;
//                } else {
//                    System.out.println("Duplicate.");
//                    retryCount = 0;
//                    throw new SocketTimeoutException();
//                }
//            } catch (SocketTimeoutException e) {
//                System.out.println("Timeout.");
//                try {
//                    sendSocket.send(sendAck);
//                } catch (IOException e1) {
//                    System.err.println("Error sending...");
//                }
//            } catch (IOException e) {
//                System.err.println("IO Error.");
//            } finally {
//                try {
//                    sendSocket.setSoTimeout(0);
//                } catch (SocketException e) {
//                    System.err.println("Error resetting Timeout.");
//                }
//            }
//        }
//    }
//
//    private boolean SendDataAndReceiveAck(DatagramSocket sendSocket, DatagramPacket sender, short blockNum) {
//        int retryCount = 0;
//        byte[] rec = new byte[ConstTFTP.BUFFER_SIZE];
//        DatagramPacket receiver = new DatagramPacket(rec, rec.length);
//
//        while (true) {
//            if (retryCount >= 6) {
//                System.err.println("Timed out. Closing connection.");
//                return false;
//            }
//            try {
//                sendSocket.send(sender);
//                System.out.println("Sent.");
//                sendSocket.setSoTimeout(((int) Math.pow(2, retryCount++)) * 1000);
//                sendSocket.receive(receiver);
//
//                short ack = getAck(receiver);
//                if (ack == blockNum) {
//                    return true;
//                } else if (ack == -1) {
//                    return false;
//                } else {
//                    retryCount = 0;
//                    throw new SocketTimeoutException();
//                }
//            } catch (SocketTimeoutException e) {
//                System.out.println("Timeout. Resending.");
//            } catch (IOException e) {
//                System.err.println("IO Error. Resending.");
//            } finally {
//                try {
//                    sendSocket.setSoTimeout(0);
//                } catch (SocketException e) {
//                    System.err.println("Error resetting Timeout.");
//                }
//            }
//        }
//    }
//
//    private DatagramPacket ackPacket(short block) {
//        ByteBuffer buffer = ByteBuffer.allocate(ConstTFTP.BUFFER_SIZE);
//        buffer.putShort(Opcode.OP_ACK.getCode());
//        buffer.putShort(block);
//
//        return new DatagramPacket(buffer.array(), 4);
//    }
//
//    private DatagramPacket dataPacket(short block, byte[] data, int length) {
//        ByteBuffer buffer = ByteBuffer.allocate(ConstTFTP.BUFFER_SIZE);
//        buffer.putShort(Opcode.OP_DAT.getCode());
//        buffer.putShort(block);
//        buffer.put(data, 0, length);
//
//        return new DatagramPacket(buffer.array(), 4 + length);
//    }
//
//    // KIỂM TRA OPCODE CỦA GÓI ACK SERVER NHẬN KHI CLIENT GỬI LÊN (DONWLOAD)
//    private short getAck(DatagramPacket ack) {
//        ByteBuffer buffer = ByteBuffer.wrap(ack.getData());
//        short opcode = buffer.getShort();
//        if (opcode == Opcode.OP_ERR.getCode()) {
//            System.err.println("Client is dead. Closing connection.");
//            parseError(buffer);
//            return -1;
//        }
//
//        return buffer.getShort();
//    }
//    // KIỂM TRA OPCODE CỦA GÓI DATA SERVER NHẬN KHI CLIENT GỬI LÊN (UPLOAD)
//    private short getData(DatagramPacket data) {
//        ByteBuffer buffer = ByteBuffer.wrap(data.getData());
//        short opcode = buffer.getShort();
//        if (opcode == Opcode.OP_ERR.getCode()) {
//            System.err.println("Client is dead. Closing connection.");
//            parseError(buffer);
//            return -1;
//        }
//
//        return buffer.getShort();
//    }
//
//    private void sendError(DatagramSocket sendSocket, short errorCode, String errMsg) {
//        ByteBuffer wrap = ByteBuffer.allocate(ConstTFTP.BUFFER_SIZE);
//        wrap.putShort(Opcode.OP_ERR.getCode());
//        wrap.putShort(errorCode);
//        wrap.put(errMsg.getBytes());
//        wrap.put((byte) 0);
//
//        DatagramPacket receivePacket = new DatagramPacket(wrap.array(), wrap.array().length);
//        try {
//            sendSocket.send(receivePacket);
//        } catch (IOException e) {
//            System.err.println("Problem sending error packet.");
//            e.printStackTrace();
//        }
//    }
//
//    private void parseError(ByteBuffer buffer) {
//        short errCode = buffer.getShort();
//
//        byte[] buf = buffer.array();
//        for (int i = 4; i < buf.length; i++) {
//            if (buf[i] == 0) {
//                String msg = new String(buf, 4, i - 4);
//                if (errCode > 7) errCode = 0;
//                System.err.println(ClientResponseErrorCode.getByCode(errCode).getMessage() + ": " + msg);
//                break;
//            }
//        }
//    }
}