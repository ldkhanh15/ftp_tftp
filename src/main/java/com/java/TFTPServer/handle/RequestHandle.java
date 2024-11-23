package com.java.TFTPServer.handle;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

public interface RequestHandle {
    InetSocketAddress receiveFrom(DatagramSocket socket, byte[] buf);
    short ParseRQ(byte[] buf, StringBuffer requestedFile);
    void HandleRQ(DatagramSocket sendSocket, String fileName, int reqType);
}
