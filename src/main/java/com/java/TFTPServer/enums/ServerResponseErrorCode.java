package com.java.TFTPServer.enums;

public enum ServerResponseErrorCode{

    ERR_LOST((short) 0, "Lost connection."),
    ERR_FNF((short) 1, "FILE NOT FOUND."),
    ERR_ACCESS((short) 2, "Error writing file."),
    ERR_EXISTS((short) 6, "File already exists.");

    private short code;
    private String description;
    ServerResponseErrorCode(short code, String description) {
        this.code = code;
        this.description = description;
    }

    public short getCode() {
        return this.code;
    }
    public String getDescription() {
        return this.description;
    }
}
