package com.java.FTPServer.system;

import com.java.FTPServer.enums.TransferType;
import com.java.FTPServer.enums.UserStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserSession {
    private String username;
    private UserStatus status = UserStatus.NOT_LOGGED_IN;
    private String currDirectory=ConstFTP.ROOT_DIR_FOR_USER;
    private TransferType transferMode = TransferType.ASCII;
    private int dataPort;
    private String rootDirectory=ConstFTP.ROOT_DIR_FOR_USER;

    public UserSession(String username, String rootDirectory) {
        this.username = username;
        this.currDirectory = rootDirectory;
        this.rootDirectory = rootDirectory;
    }
}
