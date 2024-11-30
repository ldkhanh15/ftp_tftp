package com.java.FTPServer.system;

import com.java.FTPServer.enums.TransferType;
import com.java.FTPServer.enums.UserStatus;
import com.java.FTPServer.system.ConstFTP;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserSession {
    private String username;
    private UserStatus status = UserStatus.NOT_LOGGED_IN;
    private String currDirectory = ConstFTP.ROOT_DIR_FOR_USER;
    private TransferType transferMode = TransferType.ASCII;
    private int dataPort;
    private String rootDirectory = ConstFTP.ROOT_DIR_FOR_USER;

    // Constructors, Getters, Setters
    public UserSession(String username) {
        this.username = username;
    }

}
