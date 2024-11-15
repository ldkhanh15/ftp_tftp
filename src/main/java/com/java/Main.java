package com.java;

import com.java.FTPServer.system.Server;
import com.java.TFTPServer.system.TFTPServer;
import com.java.configuration.AppConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.net.SocketException;

public class Main {
    public static void main(String[] args) throws SocketException {
        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        TFTPServer tftpServer = context.getBean(TFTPServer.class);
        try {
            tftpServer.start();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
