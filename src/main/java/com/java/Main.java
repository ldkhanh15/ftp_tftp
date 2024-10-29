package com.java;


import com.java.FTPServer.Server;
import com.java.configuration.AppConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        Server server = context.getBean(Server.class);
        server.start();
    }
}
