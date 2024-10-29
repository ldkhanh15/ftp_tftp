package com.java.FTPServer.enums;

public enum ResponseCode {
    COMMAND_OK(200, "Command okay."),
    SYSTEM_STATUS(211, "System status, or system help reply."),
    DIRECTORY_STATUS(212, "Directory status."),
    FILE_STATUS(213, "File status."),
    SERVICE_READY(220, "Service ready for new user."),
    SERVICE_CLOSING(221, "Service closing control connection."),
    USER_LOGGED_IN(230, "User logged in, proceed."),
    NEED_PASSWORD(331, "User name okay, need password."),
    NOT_LOGGED_IN(530, "Not logged in."),
    SYNTAX_ERROR(500, "Syntax error, command unrecognized."),
    NOT_SUPPORTED(501, "Syntax error in parameters or arguments."), // Mã 501
    NOT_IMPLEMENTED(502, "Command not implemented."),
    BAD_SEQUENCE(503, "Bad sequence of commands."),
    SERVICE_NOT_AVAILABLE(421, "Service not available, closing control connection."),
    DISCONNECTED(421, "Service not available, closing control connection."),
    FILE_UNAVAILABLE(550, "Requested action not taken. File unavailable."),
    FILE_ALREADY_EXISTS(553, "Requested action not taken. File already exists."),
    PATHNAME_CREATED(257, "Pathname created."),
    USER_NOT_FOUND(430, "Invalid username or password."), // Mã 430
    CANNOT_CHANGE_DIRECTORY(550, "Failed to change directory."), // Mã 550
    CANNOT_OPEN_DATA_CONNECTION(425, "Can't open data connection."), // Mã 425
    ACTION_NOT_TAKEN(450, "Requested action not taken. File unavailable, not found, or no access."), // Mã 450
    ALREADY_EXISTS(553, "Requested action not taken. File already exists."), // Mã 553
    FILE_TYPE_SET(200, "File type set to I."); // Mã 200 cho loại file
    private final int code;
    private final String message;

    ResponseCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static ResponseCode fromCode(int code) {
        for (ResponseCode response : values()) {
            if (response.getCode() == code) {
                return response;
            }
        }
        return null;
    }
}
