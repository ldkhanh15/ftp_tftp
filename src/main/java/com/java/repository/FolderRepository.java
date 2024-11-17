package com.java.repository;

import com.java.dto.FolderDTO;
import com.java.model.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface FolderRepository extends JpaRepository<Folder, Long> {
    Optional<Folder> findByParentFolder_ItemId(Long parentId);
    Optional<Folder> findByFolderNameAndParentFolder(String folderName, Folder parentFolder);
    Optional<Folder> findByFolderNameAndParentFolderIsNull(String folderName);
    @Query("SELECT new com.java.dto.FolderDTO(f.itemId, f.folderName, f.owner.id, f.owner.username) " +
            "FROM Folder f WHERE f.folderName = :folderName " +
            "AND (f.parentFolder.itemId = :parentFolderId)")
    Optional<FolderDTO> findFolderDTOByFolderNameAndParentFolder(
            @Param("folderName") String folderName,
            @Param("parentFolderId") Long parentFolderId);
    @Query("SELECT new com.java.dto.FolderDTO(f.itemId, f.folderName, f.owner.id, f.owner.username) " +
            "FROM Folder f WHERE f.folderName = :folderName AND f.parentFolder IS NULL")
    Optional<FolderDTO> findFolderDTOByFolderNameAndParentFolderIsNull(
            @Param("folderName") String folderName);

}
