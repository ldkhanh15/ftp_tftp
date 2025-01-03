package com.java.TFTPServer.handle.impl;

import com.java.TFTPServer.enums.ClientResponseErrorCode;
import com.java.TFTPServer.enums.Opcode;
import com.java.TFTPServer.handle.ErrorHandle;
import com.java.TFTPServer.system.ConstTFTP;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;

@Component
public class ErrorHandleImpl implements ErrorHandle {

    @Override
    public void sendError(DatagramSocket sendSocket, short errorCode, String errMsg, int SIZE) {
        ByteBuffer wrap = ByteBuffer.allocate(SIZE);
        wrap.putShort(Opcode.OP_ERR.getCode());
        wrap.putShort(errorCode);
        wrap.put(errMsg.getBytes());
        wrap.put((byte) 0);

        DatagramPacket receivePacket = new DatagramPacket(wrap.array(), wrap.array().length);
        try {
            sendSocket.send(receivePacket);
        } catch (IOException e) {
            System.err.println("Problem sending error packet.");
            e.printStackTrace();
        }
    }

    @Override
    public void parseError(ByteBuffer buffer) {
        short errCode = buffer.getShort();

        byte[] buf = buffer.array();
        for (int i = 4; i < buf.length; i++) {
            if (buf[i] == 0) {
                String msg = new String(buf, 4, i - 4);
                if (errCode > 7) errCode = 0;
                System.err.println(ClientResponseErrorCode.getByCode(errCode).getMessage() + ": " + msg);
                break;
            }
        }
    }
}
