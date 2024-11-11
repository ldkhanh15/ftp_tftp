package com.java.FTPServer1.handle.impl;

import com.java.FTPServer1.enums.ResponseCode;
import com.java.FTPServer1.enums.UserStatus;
import com.java.FTPServer1.handle.AuthHandle;
import com.java.FTPServer1.system.UserSession;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;

@Component
public class AuthHandleImpl implements AuthHandle {
    @Override
    public void handleUser(String username, PrintWriter out, UserSession userSession) {
        userSession.setUsername(username);
        userSession.setStatus(UserStatus.USER_ENTERED);
        out.println(ResponseCode.NEED_PASSWORD.getResponse("Please specify the password"));
    }

    @Override
    public void handlePass(String password, PrintWriter out, UserSession userSession) {
        if(false) {
            out.println(ResponseCode.NOT_LOGGED_IN.getResponse("Login Failed"));
        }
        else {
            out.println(ResponseCode.USER_LOGGED_IN.getResponse("Login Successful"));
        }
    }

    @Override
    public void handleQuit(PrintWriter out) {

    }
}
