package com.java.repository;

import com.java.model.File;
import com.java.model.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository<File,Long> {
    Optional<File> findByFileNameAndParentFolder(String fileName, Folder parentFolder);

    @Query("""
    SELECT file FROM File file
    LEFT JOIN AccessItem ai ON ai.item = file AND ai.user.id = :userId
    WHERE file.parentFolder.itemId = :folderId
    AND (file.isPublic = true OR ai IS NOT NULL OR file.owner.id = :userId)
""")
    List<File> findFilesInFolderByUserAccess(
            @Param("folderId") Long folderId,
            @Param("userId") Long userId
    );
}
