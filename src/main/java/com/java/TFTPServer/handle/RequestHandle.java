package com.java.TFTPServer.handle;

import com.java.TFTPServer.custom.OpcodeSizeCustom;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

public interface RequestHandle {
    InetSocketAddress receiveFrom(DatagramSocket socket, byte[] buf);
    OpcodeSizeCustom ParseRQ(byte[] buf, StringBuffer requestedFile);
    void HandleRQ(DatagramSocket sendSocket, String fileName, int reqType, int SIZE);
}
