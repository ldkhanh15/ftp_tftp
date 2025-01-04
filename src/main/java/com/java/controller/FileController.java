package com.java.controller;

import com.java.model.File;
import com.java.model.Folder;
import com.java.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class FileController {
    private final FileService fileService;
    public File save(File file) {
        return fileService.save(file);
    }
    public void deleteById(Long id) {
        fileService.deleteById(id);
    }
    public Optional<File> findByPath(String path) {
        return fileService.findByPath(path);
    }
    public File getFileById(Long id) {
        return fileService.getFileById(id);
    }
    public List<File> getAll() {
        return fileService.getAll();
    }
    public File findByFileNameAndFolderParent(String fileName, Folder parent) {
        return fileService.findByFileNameAndFolderParent(fileName, parent);
    }

    public File save(String absolutePath, java.io.File uploadFile) {
        return fileService.save(absolutePath, uploadFile);
    }
}
