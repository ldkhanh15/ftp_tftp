package com.java.TFTPServer.enums;


public enum ClientResponseErrorCode {
    NOT_DEFINED((short) 0, "Not defined"),
    FILE_NOT_FOUND((short) 1, "File not found."),
    ACCESS_VIOLATION((short) 2, "Access violation."),
    DISK_FULL((short) 3, "Disk full or allocation exceeded."),
    ILLEGAL_TFTP_OPERATION((short) 4, "Illegal TFTP operation."),
    UNKNOWN_TRANSFER_ID((short) 5, "Unknown transfer ID."),
    FILE_ALREADY_EXISTS((short) 6, "File already exists."),
    NO_SUCH_USER((short) 7, "No such user.");

    private final short code;
    private final String message;

    ClientResponseErrorCode(short code, String message) {
        this.code = code;
        this.message = message;
    }

    public short getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static ClientResponseErrorCode getByCode(short code) {
        for (ClientResponseErrorCode errorCode : values()) {
            if (errorCode.getCode() == code) {
                return errorCode;
            }
        }
        return null;
    }
}
