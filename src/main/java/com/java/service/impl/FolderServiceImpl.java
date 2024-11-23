package com.java.service.impl;

import com.java.dto.FolderDTO;
import com.java.dto.UserDTO;
import com.java.enums.AccessType;
import com.java.model.Folder;
import com.java.repository.AccessRepository;
import com.java.repository.FolderRepository;
import com.java.service.FolderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.java.enums.AccessType.WRITE;

@Service
public class FolderServiceImpl implements FolderService {
    private final FolderRepository folderRepository;
    private final AccessRepository accessItemRepository;

    public FolderServiceImpl(FolderRepository folderRepository, AccessRepository accessItemRepository) {
        this.folderRepository = folderRepository;
        this.accessItemRepository = accessItemRepository;
    }

    @Override
    public Folder save(Folder folder) {
        return folderRepository.save(folder);
    }

    @Override
    public void deleteById(Long id) {
        folderRepository.deleteById(id);
    }

    @Override
    public Folder getFileById(Long id) {
        return folderRepository.findById(id).orElse(null);
    }

    @Override
    public List<Folder> getAll() {
        return folderRepository.findAll();
    }

    @Transactional
    public Optional<Folder> findFolderIdByPath(String fullPath) {
        fullPath = fullPath.replace("/", "\\");

        String rootFolderName = "ftp_root";
        String[] folderNames;

        int rootIndex = fullPath.indexOf(rootFolderName);
        if (rootIndex == -1) {
            return Optional.empty();
        }
        String relativePath = fullPath.substring(rootIndex);
        folderNames = relativePath.split("\\\\");

        if (folderNames.length == 0 || !folderNames[0].equals(rootFolderName)) {
            return Optional.empty();
        }
        Optional<Folder> currentFolderOpt = folderRepository.findByFolderNameAndParentFolderIsNull(folderNames[0]);
        if (currentFolderOpt.isEmpty()) {
            return Optional.empty();
        }

        Folder currentFolder = currentFolderOpt.get();

        for (int i = 1; i < folderNames.length; i++) {
            String folderName = folderNames[i];
            currentFolderOpt = folderRepository.findByFolderNameAndParentFolder(folderName, currentFolder);
            if (currentFolderOpt.isEmpty()) {
                return Optional.empty();
            }
            currentFolder = currentFolderOpt.get();
        }

        return Optional.of(currentFolder);
    }

    @Override
    public Optional<Folder> findFolderByFolderNameAndParentFolder(String folderName, Folder parentFolder) {
        return folderRepository.findByFolderNameAndParentFolder(folderName, parentFolder);
    }

    @Transactional
    public boolean hasAccessToFolder(String fullPath, UserDTO user, AccessType accessType) {
        fullPath = fullPath.replace("/", "\\");

        String rootFolderName = "ftp_root";
        String[] folderNames;

        int rootIndex = fullPath.indexOf(rootFolderName);
        if (rootIndex == -1) {
            return false;
        }
        String relativePath = fullPath.substring(rootIndex);
        folderNames = relativePath.split("\\\\");

        if (folderNames.length == 0 || !folderNames[0].equals(rootFolderName)) {
            return false;
        }

        Optional<FolderDTO> rootFolderOpt = folderRepository.findFolderDTOByFolderNameAndParentFolderIsNull(rootFolderName);
        if (rootFolderOpt.isEmpty()) {
            return false;
        }

        FolderDTO rootFolder = rootFolderOpt.get();
        FolderDTO currentFolder = rootFolder;

        for (int i = 1; i < folderNames.length; i++) {
            String folderName = folderNames[i];
            Optional<FolderDTO> nextFolderOpt = folderRepository.findFolderDTOByFolderNameAndParentFolder(
                    folderName, currentFolder.getItemId());
            if (nextFolderOpt.isEmpty()) {
                return false;
            }

            currentFolder = nextFolderOpt.get();

            if (i == 1 && isOwner(user, currentFolder)) {
                return true;
            }

            AccessType requiredAccess = (i == folderNames.length - 1) ? accessType : AccessType.READ;
            if (!hasAccess(user, currentFolder, requiredAccess)) {
                return false;
            }
        }

        return true;
    }

    @Transactional
    protected boolean hasAccess(UserDTO user, FolderDTO folder, AccessType accessType) {
        if (isOwner(user, folder)) {
            return true;
        }
        return accessItemRepository.findAccessItemsByFolderIdAndUserId(folder.getItemId(), user.getId()).stream()
                .anyMatch(accessItem -> checkAccessType(accessItem.getAccessType(), accessType));
    }

    private boolean checkAccessType(AccessType granted, AccessType required) {
        if (required == AccessType.READ) {
            return granted == AccessType.READ || granted == WRITE || granted == AccessType.ALL;
        } else if (required == WRITE) {
            return granted == WRITE || granted == AccessType.ALL;
        } else if (required == AccessType.ALL) {
            return granted == AccessType.ALL;
        }
        return false;
    }

    private boolean isOwner(UserDTO user, FolderDTO folder) {
        return user.getId().equals(folder.getOwnerId());
    }

}
