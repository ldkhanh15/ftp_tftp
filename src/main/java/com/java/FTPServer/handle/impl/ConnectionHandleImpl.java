package com.java.FTPServer.handle.impl;

import com.java.FTPServer.enums.ResponseCode;
import com.java.FTPServer.enums.TransferType;
import com.java.FTPServer.handle.ConnectionHandle;
import com.java.FTPServer.system.ConstFTP;
import com.java.FTPServer.system.UserSession;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

@Slf4j
@Component
@Getter
@ToString
public class ConnectionHandleImpl implements ConnectionHandle {

    private final int PORT_DATA = ConstFTP.PORT_DATA;
    private final String IP_SERVER = ConstFTP.IP_SERVER;

    private ServerSocket dataSocket;
    private Socket dataConnection;

    @Override
    public void processActiveMode(String clientConnectionData, PrintWriter out) {
        if (clientConnectionData.startsWith("|")) {
            handleEPRT(clientConnectionData, out);
        }
        else {
            handlePORT(clientConnectionData, out);
        }
    }

    @Override
    public void processPassiveMode(PrintWriter out, int dataPort) {
        String[] myIpSplit = IP_SERVER.split("\\.");
        int p1 = dataPort / 256;
        int p2 = dataPort % 256;
        out.println(ResponseCode.USER_EXIT_ACKNOWLEDGED.getResponse("Entering Passive Mode (" + myIpSplit[0] + "," + myIpSplit[1] + "," + myIpSplit[2] + ","
                + myIpSplit[3] + "," + p1 + "," + p2 + ")"));
        openDataConnectionPassive(dataPort);
    }

    @Override
    public void processTypeTransfer(String typeTransfer, PrintWriter out, UserSession userSession) {
        if (typeTransfer.equals("A")){
            userSession.setTransferMode(TransferType.ASCII);
            out.println(ResponseCode.COMMAND_OK.getResponse("Switching to ASCII mode"));
        }
        else if (typeTransfer.equals("I")){
            userSession.setTransferMode(TransferType.BINARY);
            out.println(ResponseCode.COMMAND_OK.getResponse("Switching to Binary mode"));
        }
        else {
            out.println(ResponseCode.SYNTAX_ERROR.getResponse());
        }
    }

    private void handleEPRT(String clientConnectionData, PrintWriter out){
        //|<protocol>|<host>|<port>|
        String[] parts = clientConnectionData.split("\\|");
        if (parts.length != 4) {
            out.println(ResponseCode.NOT_SUPPORTED.getResponse("Syntax error in parameters or arguments."));
            return;
        }

        String protocol = parts[1];  // 1 : ipv4, 2 : ipv6
        String hostName = parts[2];
        int port;

        try {
            port = Integer.parseInt(parts[3]);
        } catch (NumberFormatException e) {
            out.println(ResponseCode.NOT_SUPPORTED.getResponse("Syntax error in port number."));
            return;
        }

        try {
            // check ip v4 or v6
            InetAddress address = InetAddress.getByName(hostName);
            if ((protocol.equals("1") && address instanceof java.net.Inet4Address) ||
                    (protocol.equals("2") && address instanceof java.net.Inet6Address)) {
                openDataConnectionActive(hostName, port);
                System.out.println(this.getDataConnection());
                out.println(ResponseCode.COMMAND_OK.getResponse("EPRT command ok, Consider using PASV."));
            } else {
                out.println(ResponseCode.REQUEST_TIMEOUT.getResponse("Network protocol not supported"));
            }
        } catch (UnknownHostException e) {
            out.println(ResponseCode.NOT_SUPPORTED.getResponse("Syntax error in host address."));
        }
    }

    private void handlePORT(String clientConnectionData, PrintWriter out){
        // PORT: h1,h2,h3,h4,p1,p2 cho IPv4
        String[] stringSplit = clientConnectionData.split(",");
        if (stringSplit.length != 6) {
            out.println(ResponseCode.NOT_SUPPORTED.getResponse("Syntax error in host address."));
            return;
        }

        String hostName = stringSplit[0] + "." + stringSplit[1] + "." + stringSplit[2] + "." + stringSplit[3];

        int port = Integer.parseInt(stringSplit[4]) * 256 + Integer.parseInt(stringSplit[5]);
        try {
            InetAddress address = InetAddress.getByName(hostName);
            if (address instanceof java.net.Inet4Address) {
                openDataConnectionActive(hostName, port);
                out.println(ResponseCode.COMMAND_OK.getResponse("PORT command ok, Consider using PASV."));
            } else {
                out.println(ResponseCode.REQUEST_TIMEOUT.getResponse("Network protocol not supported"));
            }
        } catch (UnknownHostException e) {
            out.println(ResponseCode.NOT_SUPPORTED.getResponse("Syntax error in host address."));
        }
    }

    private void openDataConnectionActive(String ipAddress, int port) {
        try {
            //InetAddress.getByName(IP_SERVER), PORT_DATA
            dataConnection = new Socket(ipAddress, port);
            log.info("Data connection - Active Mode - established");
        } catch (IOException e) {
            log.error("Could not connect to client data socket {}", e.getMessage());
            System.err.println(e.getMessage());
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void openDataConnectionPassive(int port) {
        try {
            dataSocket = new ServerSocket(port);
            dataConnection = dataSocket.accept();
            log.info("Data connection - Passive Mode - established");
        } catch (IOException e) {
            log.error("Could not create data connection {}", e.getMessage());
        }
    }

    public void closeDataConnection() {
        try {
            dataConnection.close();
            if (dataSocket != null) {
                dataSocket.close();
            }
            log.info("Data connection was closed");
        } catch (IOException e) {
            log.error("Could not close data connection {}", e.getMessage());
        }
        dataConnection = null;
        dataSocket = null;
    }
}
