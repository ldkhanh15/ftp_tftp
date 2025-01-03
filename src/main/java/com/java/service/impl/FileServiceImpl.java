package com.java.service.impl;

import com.java.exception.DataNotFoundException;
import com.java.model.File;
import com.java.model.Folder;
import com.java.model.User;
import com.java.repository.FileRepository;
import com.java.service.FileService;
import com.java.service.FolderService;
import com.java.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FileServiceImpl implements FileService {
    private final FileRepository fileRepository;
    private final FolderService folderService;
    private final UserService userService;

    public FileServiceImpl(FileRepository fileRepository, FolderService folderService, UserService userService) {
        this.fileRepository = fileRepository;
        this.folderService = folderService;
        this.userService = userService;
    }

    @Override
    public File save(File file) {
        return fileRepository.save(file);
    }
    private String getFileType(String fileName) {
        int lastIndexOfDot = fileName.lastIndexOf('.');
        if (lastIndexOfDot > 0 && lastIndexOfDot < fileName.length() - 1) {
            return fileName.substring(lastIndexOfDot + 1).toLowerCase();
        }
        return "unknown";
    }
    @Override
    public File save(String fullPath, java.io.File file) {
        Optional<Folder> parent=folderService.findFolderIdByPath(fullPath);
        if(parent.isPresent()){
            User user=userService.findByUsername("admin");

            File newFile=new File();
            newFile.setOwner(user);
            newFile.setFileSize(file.length());
            newFile.setFileName(file.getName());
            newFile.setIsPublic(true);
            newFile.setFileType(getFileType(newFile.getFileName()));
            newFile.setParentFolder(parent.get());
            newFile.setFilePath(file.getAbsolutePath()+"/"+file.getName());
            return fileRepository.save(newFile);
        }else{
            throw new DataNotFoundException("Path not found");
        }


    }
    @Override
    public void deleteById(Long id) {
        fileRepository.deleteById(id);
    }

    @Override
    public File getFileById(Long id) {
        return fileRepository.findById(id).orElse(null);
    }

    @Override
    public List<File> getAll() {
        return fileRepository.findAll();
    }

    @Override
    public File findByFileNameAndFolderParent(String fileName, Folder parent) {
        return fileRepository.findByFileNameAndParentFolder(fileName, parent).orElse(null);
    }



}
