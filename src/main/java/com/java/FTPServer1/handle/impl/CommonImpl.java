package com.java.FTPServer1.handle.impl;

import com.java.FTPServer1.handle.CommonHandle;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;

@Component
public class CommonImpl implements CommonHandle {
    @Override
    public void listName(PrintWriter out) {

    }

    @Override
    public void listDetail(PrintWriter out) {

    }

    @Override
    public void initiateRename(String nameOnServer, PrintWriter out) {

    }

    @Override
    public void finalizeRename(String newName, PrintWriter out) {

    }
}
