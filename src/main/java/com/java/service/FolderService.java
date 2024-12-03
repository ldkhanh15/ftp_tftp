package com.java.service;

import com.java.dto.UserDTO;
import com.java.enums.AccessType;
import com.java.model.Folder;
import com.java.model.Item;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface FolderService {
    Folder save(Folder folder);
    void deleteById(Long id);
    Folder getFileById(Long id);
    List<Folder> getAll();
   Optional<Folder> findFolderIdByPath(String path);
   Optional<Folder> findFolderByFolderNameAndParentFolder(String folderName, Folder parentFolder);
   @Transactional
   boolean hasAccessToFolder(String fullPath, UserDTO user, AccessType accessType);

    List<Item> findItemByAccess(Long folderId, Long userId);
    List<Item> findItemByAdmin(Long folderId);
}
