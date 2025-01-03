package com.java.TFTPServer.system;

public interface ConstTFTP {
    public static final int PORT_TFTP = 69;
    public static final int MAX_SIZE = 65000;
    public static final String READ_ROOT = System.getProperty("user.dir") + "\\ftp_root\\public";
    public static final String WRITE_ROOT = System.getProperty("user.dir") + "\\ftp_root\\public";
    public static final String MODE_NETASCII = "netascii";
    public static final String MODE_OCTET = "octet";
}
