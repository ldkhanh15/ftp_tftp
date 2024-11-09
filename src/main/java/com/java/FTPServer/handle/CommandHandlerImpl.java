package com.java.FTPServer.handle;

import com.java.FTPServer.enums.TransferType;
import lombok.Setter;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;


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
    public void handlePort(String args, PrintWriter controlOutWriter) {
        this.controlOutWriter=controlOutWriter;
        String[] stringSplit = args.split(",");
        String hostName = stringSplit[0] + "." + stringSplit[1] + "." + stringSplit[2] + "." + stringSplit[3];
        int p = Integer.parseInt(stringSplit[4]) * 256 + Integer.parseInt(stringSplit[5]);
        openDataConnectionActive(hostName, p);
        sendMsgToClient("Command OK");
    }
    public void openDataConnectionActive(String ipAddress, int port) {
        try {
            dataConnection = new Socket(ipAddress, port);
            dataOutWriter = new PrintWriter(dataConnection.getOutputStream(), true);
            printOutput("Data connection - Active Mode - established");
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
            sendMsgToClient("OK change to ASCII");
        } else if (mode.toUpperCase().equals("I")) {
            transferMode = TransferType.BINARY;
            sendMsgToClient("OK change to binary");
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
                sendMsgToClient("Opening binary mode data connection for requested file " + f.getName());
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
                sendMsgToClient("Opening ASCII mode data connection for requested file " + f.getName());
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
            } else {
                if (transferMode == TransferType.BINARY) {
                    BufferedOutputStream fout = null;
                    BufferedInputStream fin = null;
                    sendMsgToClient("Opening binary mode data connection for requested file " + f.getName());
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
                    sendMsgToClient("Opening ASCII mode data connection for requested file " + f.getName());
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
