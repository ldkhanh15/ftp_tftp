package com.java.TFTPServer.system;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;

import com.java.FTPServer.system.UserSession;
import com.java.TFTPServer.custom.NetAsciiInputStream;
import com.java.TFTPServer.custom.NetAsciiOutputStream;
import com.java.TFTPServer.enums.ClientResponseErrorCode;
import com.java.TFTPServer.enums.Opcode;
import com.java.TFTPServer.enums.ServerResponseErrorCode;
import com.java.TFTPServer.handle.DataAndAckHandle;
import com.java.TFTPServer.handle.ErrorHandle;
import com.java.TFTPServer.handle.RequestHandle;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TFTPServer {
    @Setter
    private UserSession userSession;
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
                            requestHandle.HandleRQ(sendSocket, requestedFile.toString(), Opcode.OP_RRQ.getCode());
                        } else {
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
}