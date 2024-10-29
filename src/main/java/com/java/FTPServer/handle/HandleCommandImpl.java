package com.java.FTPServer.handle;

import com.java.FTPServer.Const;
import com.java.FTPServer.enums.ResponseCode;
import com.java.controller.UserController;
import com.java.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.Socket;

@RequiredArgsConstructor
@Component
public class HandleCommandImpl implements HandleCommand {
    private final UserController userController;
    private final String rootDir = Const.ROOT_DIR;

    @Override
    public void storeFile(String filename, Socket dataSocket, PrintWriter out) {

    }

    @Override
    public void login(String[] commandParts, String username, PrintWriter out) {
        String password = commandParts[1];
        boolean loginSuccessful = userController.login(username, password, Role.USER);
        if (loginSuccessful) {
            out.println(ResponseCode.USER_LOGGED_IN);
        } else {
            out.println(ResponseCode.NOT_LOGGED_IN);
        }
    }

}
