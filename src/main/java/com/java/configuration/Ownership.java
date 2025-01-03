package com.java.configuration;

import com.java.FTPServer.anotation.FolderOwnerShip;
import com.java.FTPServer.system.UserSession;
import com.java.FTPServer.ulti.UserSessionManager;
import com.java.dto.UserDTO;
import com.java.enums.AccessType;
import com.java.enums.Role;
import com.java.exception.PermissionException;
import com.java.model.User;
import com.java.service.FolderService;
import com.java.service.UserService;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;

@Aspect
@Component
public class Ownership {

    private final UserService userService;
    private final FolderService folderService;

    private static final String ROLE_ADMIN = "ADMIN";

    public Ownership(UserService userService, FolderService folderService) {
        this.userService = userService;
        this.folderService = folderService;
    }

    @Before("@annotation(folderOwnerShip) && args(out, ..)")
    public void checkFolderOwnership(FolderOwnerShip folderOwnerShip, PrintWriter out) throws Exception {
        UserSession userSession=UserSessionManager.getUserSession();
        String username = "";
        if(userSession!=null){
            username=userSession.getUsername();
        }else{
            username="anonymous";
        }
        System.out.println("username: "+username);
        String currentDirectory=userSession.getCurrDirectory();
        if (username == null) {
            throw new Exception("User is not authenticated.");
        }

        UserDTO user=userService.findByUserNameDTO(username);
        if (user == null) {
            throw new Exception("User not found.");
        }

        AccessType accessType = folderOwnerShip.action();
        System.out.println(ROLE_ADMIN);
        System.out.println(user.getRole());
        if (Role.ADMIN.equals(user.getRole())) {
            System.out.println("User is an admin. Access granted.");
            return;
        }

        if (!folderService.hasAccessToFolder(currentDirectory, user, accessType)) {
            out.println("550 Permission denied");
            throw new PermissionException("User does not permission");
        }

        System.out.println("Access granted.");
    }
}
