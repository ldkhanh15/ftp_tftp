package com.java.FTPServer.handle.impl;

import com.java.FTPServer.enums.ResponseCode;
import com.java.FTPServer.enums.UserStatus;
import com.java.FTPServer.handle.AuthHandle;
import com.java.FTPServer.system.UserSession;
import com.java.FTPServer.ulti.UserSessionManager;
import com.java.FTPServer.ulti.UserStore;
import com.java.controller.UserController;
import com.java.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;

@Component
@RequiredArgsConstructor
public class AuthHandleImpl implements AuthHandle {
    private final UserController userController;
    @Override
    public void handleUser(String username, PrintWriter out, UserSession userSession) {
        userSession.setUsername(username);
        userSession.setStatus(UserStatus.USER_ENTERED);
        UserSession userSession1 = UserSessionManager.getUserSession();
        userSession1.setUsername(username);
        if(username.equalsIgnoreCase("anonymous")){
            out.println(ResponseCode.USER_LOGGED_IN.getResponse("Login Successful"));
            //UserSessionManager.setUserSession(userSession1);
        }else{
            UserSessionManager.setUserSession(userSession1);
            out.println(ResponseCode.NEED_PASSWORD.getResponse("Please specify the password"));
        }


    }

    @Override
    public void handlePass(String password, PrintWriter out, UserSession userSession) {
        if(userController.login(userSession.getUsername(), password)) {
            userSession.setStatus(UserStatus.LOGGED_IN);
            out.println(ResponseCode.USER_LOGGED_IN.getResponse("Login Successful"));
        }
        else {
            out.println(ResponseCode.NOT_LOGGED_IN.getResponse("Login Failed"));
        }
    }

    @Override
    public void handleQuit(PrintWriter out) {
        out.println(ResponseCode.SERVICE_CLOSING.getResponse());
        try {
            out.close();
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
