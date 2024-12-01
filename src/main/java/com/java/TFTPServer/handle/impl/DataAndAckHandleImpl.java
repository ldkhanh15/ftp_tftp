package com.java.TFTPServer.handle.impl;

import com.java.TFTPServer.enums.Opcode;
import com.java.TFTPServer.handle.DataAndAckHandle;
import com.java.TFTPServer.handle.ErrorHandle;
import com.java.TFTPServer.system.ConstTFTP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;

@Component
public class DataAndAckHandleImpl implements DataAndAckHandle {

    private final ErrorHandle errorHandle;
    @Autowired
    public DataAndAckHandleImpl(ErrorHandle errorHandle) {
        this.errorHandle = errorHandle;
    }
    @Override
    public DatagramPacket ResponseACKAndReceiveData(DatagramSocket sendSocket, DatagramPacket sendAck, short block){
        int retryCount = 0;
        byte[] rec = new byte[ConstTFTP.BUFFER_SIZE];
        DatagramPacket receiver = new DatagramPacket(rec, rec.length);

        while (true) {
            if (retryCount >= 6) {
                System.err.println("Timed out. Closing connection.");
                return null;
            }
            try {
                System.out.println("sending ack for block: " + block);
                sendSocket.send(sendAck);
                sendSocket.setSoTimeout(((int) Math.pow(2, retryCount++)) * 2000);
                sendSocket.receive(receiver);

                short blockNum = getData(receiver);
                System.out.println("Block received: " + blockNum + ", Block expected: " + block);
                if (blockNum == block) {
                    return receiver;
                } else if (blockNum == -1) {
                    return null;
                } else {
                    System.out.println("Duplicate.");
                    retryCount = 0;
                    throw new SocketTimeoutException();
                }
            } catch (SocketTimeoutException e) {
                System.out.println("Timeout.");
                try {
                    sendSocket.send(sendAck);
                } catch (IOException e1) {
                    System.err.println("Error sending...");
                }
            } catch (IOException e) {
                System.err.println("IO Error.");
            } finally {
                try {
                    sendSocket.setSoTimeout(0);
                } catch (SocketException e) {
                    System.err.println("Error resetting Timeout.");
                }
            }
        }
    }
    @Override
    public boolean SendDataAndReceiveAck(DatagramSocket sendSocket, DatagramPacket sender, short blockNum){
        int retryCount = 0;
        byte[] rec = new byte[ConstTFTP.BUFFER_SIZE];
        DatagramPacket receiver = new DatagramPacket(rec, rec.length);

        while (true) {
            if (retryCount >= 6) {
                System.err.println("Timed out. Closing connection.");
                return false;
            }
            try {
                sendSocket.send(sender);
                System.out.println("Sent.");
                sendSocket.setSoTimeout(((int) Math.pow(2, retryCount++)) * 50000);
                System.out.println("Thoi gian: "+((int) Math.pow(2, retryCount++)) * 50000);
                sendSocket.receive(receiver);

                short ack = getAck(receiver);
                if (ack == blockNum) {
                    return true;
                } else if (ack == -1) {
                    return false;
                } else {
                    retryCount = 0;
                    throw new SocketTimeoutException();
                }
            } catch (SocketTimeoutException e) {
                System.out.println("Timeout. Resending.");
            } catch (IOException e) {
                System.err.println("IO Error. Resending.");
            } finally {
                try {
                    sendSocket.setSoTimeout(0);
                } catch (SocketException e) {
                    System.err.println("Error resetting Timeout.");
                }
            }
        }
    }
    public DatagramPacket ackPacket(short block){
        ByteBuffer buffer = ByteBuffer.allocate(ConstTFTP.BUFFER_SIZE);
        buffer.putShort(Opcode.OP_ACK.getCode());
        buffer.putShort(block);

        return new DatagramPacket(buffer.array(), 4);
    }
    public DatagramPacket dataPacket(short block, byte[] data, int length){
        ByteBuffer buffer = ByteBuffer.allocate(ConstTFTP.BUFFER_SIZE);
        buffer.putShort(Opcode.OP_DAT.getCode());
        buffer.putShort(block);
        buffer.put(data, 0, length);

        return new DatagramPacket(buffer.array(), 4 + length);
    }
    public short getAck(DatagramPacket ack){
        ByteBuffer buffer = ByteBuffer.wrap(ack.getData());
        short opcode = buffer.getShort();
        if (opcode == Opcode.OP_ERR.getCode()) {
            System.err.println("Client is dead. Closing connection.");
            errorHandle.parseError(buffer);
            return -1;
        }

        return buffer.getShort();
    }

    public short getData(DatagramPacket data){
        ByteBuffer buffer = ByteBuffer.wrap(data.getData());
        short opcode = buffer.getShort();
        if (opcode == Opcode.OP_ERR.getCode()) {
            System.err.println("Client is dead. Closing connection.");
            errorHandle.parseError(buffer);
            return -1;
        }

        return buffer.getShort();
    }
}
