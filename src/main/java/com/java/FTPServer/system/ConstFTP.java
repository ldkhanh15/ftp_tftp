package com.java.FTPServer.system;

public interface ConstFTP {
   String ROOT_DIR = "ftp_root";
   String ROOT_DIR_FOR_USER = System.getProperty("user.dir") + "\\ftp_root";
   int PORT = 21;
   int PORT_DATA = 20;
   String IP_SERVER = "127.0.0.1";
}
