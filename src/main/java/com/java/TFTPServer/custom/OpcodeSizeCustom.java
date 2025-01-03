package com.java.TFTPServer.custom;

public class OpcodeSizeCustom {

    private short opCode;
    private int size;

    public OpcodeSizeCustom(short opCode, int size) {
        this.opCode = opCode;
        this.size = size;
    }

    public int getOpcode() {
        return opCode;
    }

    public int getSize() {
        return size;
    }

    public void setOpcode(int opCode) {
        this.opCode = (short) opCode;
    }
    public void setSize(int size) {
        this.size = size;
    }

}
