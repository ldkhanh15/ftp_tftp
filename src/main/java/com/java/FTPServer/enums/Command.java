package com.java.FTPServer.enums;

public enum Command {
    USER,
    PASS,
    QUIT,
    PORT,
    EPRT,
    EPSV,
    TYPE,
    STOR,
    RETR,
    APPE,
    DELE,
    LIST,
    NLIST,
    RNFR,
    RNTO,
    CWD,
    XPWD,
    XMKD,
    XRMD;

    public static Command fromString(String command) {
        try {
            return Command.valueOf(command.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
