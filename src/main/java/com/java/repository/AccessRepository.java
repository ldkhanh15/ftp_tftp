package com.java.repository;

import com.java.model.AccessItem;
import com.java.model.Folder;
import com.java.model.Item;
import com.java.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AccessRepository extends JpaRepository<AccessItem,Long> {
    @Query("SELECT ai FROM AccessItem ai WHERE ai.item.itemId = :folderId AND ai.user.id = :userId")
    List<AccessItem> findAccessItemsByFolderIdAndUserId(@Param("folderId") Long folderId, @Param("userId") Long userId);
}
