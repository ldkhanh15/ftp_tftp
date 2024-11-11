package com.java.FTPServer.system;

import com.java.FTPServer.enums.TransferType;
import com.java.FTPServer.enums.UserStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSession {
    private String username;
    private UserStatus status = UserStatus.NOT_LOGGED_IN;
    private String currDirectory = System.getProperty("user.dir") + "/ftp_root";
    private TransferType transferMode = TransferType.ASCII;
    private int dataPort;
}
