package com.java.TFTPServer.enums;

public enum Opcode {
    OP_RRQ((short) 1),
    OP_WRQ((short) 2),
    OP_DAT((short) 3),
    OP_ACK((short) 4),
    OP_ERR((short) 5);

    private final short code;

    Opcode(short code) {
        this.code = code;
    }

    public short getCode() {
        return code;
    }

    public static Opcode getByCode(short code) {
        for (Opcode opcode : values()) {
            if (opcode.getCode() == code) {
                return opcode;
            }
        }
        return null;
    }
}
