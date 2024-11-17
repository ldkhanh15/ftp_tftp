package com.java.repository;

import com.java.model.File;
import com.java.model.Folder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FileRepository extends JpaRepository<File,Long> {
    Optional<File> findByFileNameAndParentFolder(String fileName, Folder parentFolder);
}
