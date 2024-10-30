package com.java.repository;

import com.java.model.Folder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FolderRepository extends JpaRepository<Folder, Long> {
    Optional<Folder> findByParentFolder_ItemId(Long parentId);
}
