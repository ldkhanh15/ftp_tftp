package com.java.service.impl;

import com.java.model.File;
import com.java.model.Folder;
import com.java.repository.FileRepository;
import com.java.service.FileService;
import com.java.service.FolderService;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class FileServiceImpl implements FileService {
    private final FileRepository fileRepository;
    private final FolderService folderService;

    public FileServiceImpl(FileRepository fileRepository, FolderService folderService) {
        this.fileRepository = fileRepository;
        this.folderService = folderService;
    }

    @Override
    public File save(File file) {
        return fileRepository.save(file);
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
