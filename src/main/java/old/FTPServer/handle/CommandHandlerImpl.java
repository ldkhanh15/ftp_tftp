package old.FTPServer.handle;

import lombok.Setter;
import old.FTPServer.enums.TransferType;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

@Component
public class CommandHandlerImpl {
    private ServerSocket dataSocket;
    private Socket dataConnection;
    private PrintWriter dataOutWriter;
    private String currDirectory;
    private PrintWriter controlOutWriter;
    @Setter
    private int dataPort;
    private TransferType transferMode = TransferType.ASCII;

    public CommandHandlerImpl(){
        this.currDirectory = System.getProperty("user.dir") + "/ftp_root";
    }
    public void handlePasv(PrintWriter controlOutWriter) {
        this.controlOutWriter=controlOutWriter;
        String myIp = "127.0.0.1";
        String myIpSplit[] = myIp.split("\\.");
        int p1 = dataPort / 256;
        int p2 = dataPort % 256;
        sendMsgToClient("Entering Passive Mode (" + myIpSplit[0] + "," + myIpSplit[1] + "," + myIpSplit[2] + ","
                + myIpSplit[3] + "," + p1 + "," + p2 + ")");
        openDataConnectionPassive(dataPort);
    }
    public void openDataConnectionPassive(int port) {
        try {
            dataSocket = new ServerSocket(port);
            dataConnection = dataSocket.accept();
            dataOutWriter = new PrintWriter(dataConnection.getOutputStream(), true);
            printOutput("Data connection - Passive Mode - established");
        } catch (IOException e) {
            printOutput("Could not create data connection.");
            e.printStackTrace();
        }
    }
//    public void handlePort(String args, PrintWriter controlOutWriter) {
//        this.controlOutWriter=controlOutWriter;
//        String[] stringSplit = args.split(",");
//        String hostName = stringSplit[0] + "." + stringSplit[1] + "." + stringSplit[2] + "." + stringSplit[3];
//        int p = Integer.parseInt(stringSplit[4]) * 256 + Integer.parseInt(stringSplit[5]);
//        openDataConnectionActive(hostName, p);
//        sendMsgToClient("Command OK");
//    }
    public void handlePort(String args, PrintWriter controlOutWriter) {
    this.controlOutWriter = controlOutWriter;

    if (args.startsWith("|")) {
        // Xử lý định dạng EPRT: |<protocol>|<host>|<port>|
        String[] parts = args.split("\\|");
        if (parts.length != 4) {
            sendMsgToClient("501 Syntax error in parameters or arguments.");
            return;
        }

        String protocol = parts[1];  // "1" là IPv4, "2" là IPv6
        String hostName = parts[2];
        int port;

        try {
            port = Integer.parseInt(parts[3]);
        } catch (NumberFormatException e) {
            sendMsgToClient("501 Syntax error in port number.");
            return;
        }

        try {
            // Kiểm tra địa chỉ là IPv4 hoặc IPv6
            InetAddress address = InetAddress.getByName(hostName);
            if ((protocol.equals("1") && address instanceof java.net.Inet4Address) ||
                    (protocol.equals("2") && address instanceof java.net.Inet6Address)) {
                openDataConnectionActive(hostName, port);
                sendMsgToClient("200 Command OK");
            } else {
                sendMsgToClient("522 Network protocol not supported.");
            }
        } catch (UnknownHostException e) {
            sendMsgToClient("501 Syntax error in host address.");
        }
    } else {
        // Xử lý định dạng PORT: h1,h2,h3,h4,p1,p2 cho IPv4
        String[] stringSplit = args.split(",");
        if (stringSplit.length != 6) {
            sendMsgToClient("501 Syntax error in parameters or arguments.");
            return;
        }

        String hostName = stringSplit[0] + "." + stringSplit[1] + "." + stringSplit[2] + "." + stringSplit[3];
        int port = Integer.parseInt(stringSplit[4]) * 256 + Integer.parseInt(stringSplit[5]);

        try {
            InetAddress address = InetAddress.getByName(hostName);
            if (address instanceof java.net.Inet4Address) {
                openDataConnectionActive(hostName, port);
                sendMsgToClient("200 Command OK");
            } else {
                sendMsgToClient("522 Network protocol not supported for PORT command.");
            }
        } catch (UnknownHostException e) {
            sendMsgToClient("501 Syntax error in host address.");
        }
    }
}
    public void openDataConnectionActive(String ipAddress, int port) {
        try {
            dataConnection = new Socket(ipAddress, port);
            dataOutWriter = new PrintWriter(dataConnection.getOutputStream(), true);
            printOutput("150 Data connection - Active Mode - established");
        } catch (IOException e) {
            printOutput("Could not connect to client data socket");
            e.printStackTrace();
        }

    }
    public void sendMsgToClient(String msg) {
        controlOutWriter.println(msg);
    }
    public void closeDataConnection() {
        try {
            dataOutWriter.close();
            dataConnection.close();
            if (dataSocket != null) {
                dataSocket.close();
            }
            printOutput("Data connection was closed");
        } catch (IOException e) {
            printOutput("Could not close data connection");
            e.printStackTrace();
        }
        dataOutWriter = null;
        dataConnection = null;
        dataSocket = null;
    }
    public void handleType(String mode, PrintWriter controlOutWriter) {
        this.controlOutWriter=controlOutWriter;
        if (mode.toUpperCase().equals("A")) {
            transferMode = TransferType.ASCII;
            sendMsgToClient("200 OK change to ASCII");
        } else if (mode.toUpperCase().equals("I")) {
            transferMode = TransferType.BINARY;
            sendMsgToClient("200 OK change to binary");
        } else
            sendMsgToClient("Not OK");

    }
    public void handleRetr(String file, PrintWriter controlOutWriter) {
        this.controlOutWriter=controlOutWriter;
        File f = new File(currDirectory + "/" + file);
        if (!f.exists()) {
            sendMsgToClient("File does not exist");
        } else {
            if (transferMode == TransferType.BINARY) {
                BufferedOutputStream fout = null;
                BufferedInputStream fin = null;
                sendMsgToClient("150 Opening binary mode data connection for requested file " + f.getName());
                try {
                    fout = new BufferedOutputStream(dataConnection.getOutputStream());
                    fin = new BufferedInputStream(new FileInputStream(f));
                } catch (Exception e) {
                    printOutput("Could not create file streams");
                }
                printOutput("Starting file transmission of " + f.getName());
                byte[] buf = new byte[1024];
                int l = 0;
                try {
                    while ((l = fin.read(buf, 0, 1024)) != -1) {
                        fout.write(buf, 0, l);
                    }
                } catch (IOException e) {
                    printOutput("Could not read from or write to file streams");
                    e.printStackTrace();
                }
                try {
                    fin.close();
                    fout.close();
                } catch (IOException e) {
                    printOutput("Could not close file streams");
                    e.printStackTrace();
                }
                printOutput("Completed file transmission of " + f.getName());
                sendMsgToClient("File transfer successful. Closing data connection.");
            }
            else {
                sendMsgToClient("150 opening ASCII mode data connection for requested file " + f.getName());
                BufferedReader rin = null;
                PrintWriter rout = null;
                try {
                    rin = new BufferedReader(new FileReader(f));
                    rout = new PrintWriter(dataConnection.getOutputStream(), true);
                } catch (IOException e) {
                    printOutput("Could not create file streams");
                }
                try {
                    String s="";
                    while ((s = rin.readLine()) != null) {
                        rout.println(s);
                    }
                } catch (IOException e) {
                    printOutput("Could not read from or write to file streams");
                    e.printStackTrace();
                }
                try {
                    rout.close();
                    rin.close();
                } catch (IOException e) {
                    printOutput("Could not close file streams");
                    e.printStackTrace();
                }
                sendMsgToClient("File transfer successful. Closing data connection.");
            }
        }
        closeDataConnection();

    }
    public void handleStor(String file, PrintWriter controlOutWriter) {
        this.controlOutWriter=controlOutWriter;
        if (file == null) {
            sendMsgToClient("No filename given");
        } else {
            File f = new File(currDirectory + "/" + file);
            System.out.println(currDirectory + "/" + file);
            if (f.exists()) {
                sendMsgToClient("File already exists");
            }
            else {
                if (transferMode == TransferType.BINARY) {
                    BufferedOutputStream fout = null;
                    BufferedInputStream fin = null;
                    sendMsgToClient("150 Opening binary mode data connection for requested file " + f.getName());
                    try {
                        fout = new BufferedOutputStream(new FileOutputStream(f));
                        fin = new BufferedInputStream(dataConnection.getInputStream());
                    } catch (Exception e) {
                        printOutput("Could not create file streams");
                    }
                    printOutput("Start receiving file " + f.getName());

                    byte[] buf = new byte[1024];
                    int l = 0;
                    try {
                        while ((l = fin.read(buf, 0, 1024)) != -1) {
                            fout.write(buf, 0, l);
                        }
                    } catch (IOException e) {
                        printOutput("Could not read from or write to file streams");
                        e.printStackTrace();
                    }
                    try {
                        fin.close();
                        fout.close();
                    } catch (IOException e) {
                        printOutput("Could not close file streams");
                        e.printStackTrace();
                    }

                    printOutput("Completed receiving file " + f.getName());

                    sendMsgToClient("File transfer successful. Closing data connection.");

                }

                else {
                    sendMsgToClient("150 Opening ASCII mode data connection for requested file " + f.getName());
                    BufferedReader rin = null;
                    PrintWriter rout = null;
                    try {
                        rin = new BufferedReader(new InputStreamReader(dataConnection.getInputStream()));
                        rout = new PrintWriter(new FileOutputStream(f), true);

                    } catch (IOException e) {
                        printOutput("Could not create file streams");
                    }
                    try {
                        String s="";
                        while ((s = rin.readLine()) != null) {
                            rout.println(s);
                        }
                    } catch (IOException e) {
                        printOutput("Could not read from or write to file streams");
                        e.printStackTrace();
                    }

                    try {
                        rout.close();
                        rin.close();
                    } catch (IOException e) {
                        printOutput("Could not close file streams");
                        e.printStackTrace();
                    }
                    sendMsgToClient("File transfer successful. Closing data connection.");
                }

            }
            closeDataConnection();
        }

    }
    private void printOutput(String msg) {
        System.out.println(msg);
    }
}
