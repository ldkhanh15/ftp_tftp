package com.java.controller;

import com.java.dto.FolderDTO;
import com.java.dto.UserDTO;
import com.java.enums.AccessType;
import com.java.exception.DataNotFoundException;
import com.java.model.Folder;
import com.java.service.FolderService;
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
    public Folder save(String fullPath, String folderName) {
        Optional<Folder> parentFolder=folderService.findFolderIdByPath(fullPath);
        if(parentFolder.isPresent()){
            Folder folder = new Folder();
            folder.setFolderName(folderName);
            folder.setParentFolder(parentFolder.get());
            folder.setIsPublic(true);
            return folderService.save(folder);
        }else{
            throw new DataNotFoundException("Path not found");
        }
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

    public Optional<Folder> findFolderByFolderNameAndParentFolder(String folderName, Folder parentFolder) {
        return folderService.findFolderByFolderNameAndParentFolder(folderName, parentFolder);
    }

    public boolean hasAccessToFolder(String fullPath, UserDTO user, AccessType accessType) {
        return folderService.hasAccessToFolder(fullPath, user, accessType);
    }

}
