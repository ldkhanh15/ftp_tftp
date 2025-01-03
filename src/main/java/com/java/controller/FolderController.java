package com.java.controller;

import com.java.FTPServer.system.UserSession;
import com.java.FTPServer.ulti.UserSessionManager;
import com.java.dto.FolderDTO;
import com.java.dto.UserDTO;
import com.java.enums.AccessType;
import com.java.exception.DataNotFoundException;
import com.java.model.Folder;
import com.java.model.User;
import com.java.service.FolderService;
import com.java.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.java.enums.AccessType.WRITE;

@RequiredArgsConstructor
@Component
public class FolderController {
    private final FolderService folderService;
    private final UserService userService;
    public Folder save(String fullPath, String folderName, String username) {
        // Use "admin" as the default username if none is provided
        if (username == null || username.isEmpty()) {
            username = "admin";
        }

        // Find the parent folder by its path
        Optional<Folder> parentFolder = folderService.findFolderIdByPath(fullPath);
        if (parentFolder.isEmpty()) {
            throw new DataNotFoundException("Path not found");
        }

        // Find the user by username
        User user = userService.findByUsername(username);
        if (user == null) {
            throw new DataNotFoundException("User not found");
        }

        // Create and save the new folder
        Folder folder = new Folder();
        folder.setOwner(user);
        folder.setFolderName(folderName);
        folder.setParentFolder(parentFolder.get());
        folder.setIsPublic(true);

        return folderService.save(folder);
    }

    public Folder save(Folder folder){
        return folderService.save(folder);
    }
    public void deleteById(Long id) {
        folderService.deleteById(id);
    }

    public Folder getFileById(Long id) {
        return folderService.getFileById(id);
    }

    public List<Folder> getAll() {
        return folderService.getAll();
    }

    public Optional<Folder> findFolderIdByPath(String fullPath) {
        return folderService.findFolderIdByPath(fullPath);
    }
    public Optional<Folder> findFolderParentByPath(String fullPath) {
        return folderService.findFolderParentByPath(fullPath);
    }

    public Optional<Folder> findFolderByFolderNameAndParentFolder(String folderName, Folder parentFolder) {
        return folderService.findFolderByFolderNameAndParentFolder(folderName, parentFolder);
    }

    public boolean hasAccessToFolder(String fullPath, UserDTO user, AccessType accessType) {
        return folderService.hasAccessToFolder(fullPath, user, accessType);
    }

}
