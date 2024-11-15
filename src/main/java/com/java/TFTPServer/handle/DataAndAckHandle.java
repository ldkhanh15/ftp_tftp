package com.java.TFTPServer.handle;

import com.java.TFTPServer.enums.Opcode;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;

public interface DataAndAckHandle {
    DatagramPacket ResponseACKAndReceiveData(DatagramSocket sendSocket, DatagramPacket sendAck, short block);
    boolean SendDataAndReceiveAck(DatagramSocket sendSocket, DatagramPacket sender, short blockNum);
    DatagramPacket ackPacket(short block);
    DatagramPacket dataPacket(short block, byte[] data, int length);
    short getAck(DatagramPacket ack);
    short getData(DatagramPacket data);

}