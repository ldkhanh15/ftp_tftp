package com.java.service;

import com.java.model.File;
import com.java.model.Folder;

import java.util.List;
import java.util.Optional;

public interface FileService {
    File save(File folder);
    void deleteById(Long id);
    File getFileById(Long id);
    List<File> getAll();
    File findByFileNameAndFolderParent(String fileName, Folder parent);

    File save(String fullPath, java.io.File file);
}
