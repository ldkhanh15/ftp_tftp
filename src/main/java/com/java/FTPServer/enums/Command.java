package com.java.FTPServer.enums;

public enum Command {
    USER,
    PASS,
    QUIT,
    PORT,
    EPRT,
    PASV,
    EPSV,
    TYPE,
    STOR,
    RETR,
    APPE,
    DELE,
    LIST,
    NLST,
    RNFR,
    RNTO,
    CWD,
    XPWD,
    XMKD,
    XRMD,
    PER,
    CPER,
    DPER,
    PUB;


    public static Command fromString(String command) {
        try {
            return Command.valueOf(command.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
