package com.java.FTPServer1.system;

import com.java.FTPServer1.enums.TransferType;
import com.java.FTPServer1.enums.UserStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSession {
    private String username;
    private UserStatus status = UserStatus.NOT_LOGGED_IN;
    private String currDirectory = System.getProperty("user.dir") + "/ftp_root";
    private final TransferType transferMode = TransferType.ASCII;
    private int dataPort;
}
