package com.java.FTPServer.handle;

import java.io.PrintWriter;

public interface PermissionHandle {
    void handleGetPermission(PrintWriter out,Long itemId);
    void handleAddPermission(PrintWriter out,String value);
    void handleDelPermission(PrintWriter out,String value);
    void handleChangePublic(PrintWriter out,String value);
}
