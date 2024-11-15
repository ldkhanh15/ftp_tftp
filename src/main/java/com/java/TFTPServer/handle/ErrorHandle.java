package com.java.TFTPServer.handle;

import com.java.TFTPServer.enums.ClientResponseErrorCode;
import com.java.TFTPServer.enums.Opcode;
import com.java.TFTPServer.system.ConstTFTP;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;

public interface ErrorHandle {
    void sendError(DatagramSocket sendSocket, short errorCode, String errMsg);
    void parseError(ByteBuffer buffer);
}
