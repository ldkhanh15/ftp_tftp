package com.java.TFTPServer.handle;

import com.java.TFTPServer.enums.Opcode;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;

public interface DataAndAckHandle {
    DatagramPacket ResponseACKAndReceiveData(DatagramSocket sendSocket, DatagramPacket sendAck, short block, int SIZE);
    boolean SendDataAndReceiveAck(DatagramSocket sendSocket, DatagramPacket sender, short blockNum, int SIZE);
    DatagramPacket ackPacket(short block, int SIZE);
    DatagramPacket dataPacket(short block, byte[] data, int length, int SIZE);
    short getAck(DatagramPacket ack);
    short getData(DatagramPacket data);

}