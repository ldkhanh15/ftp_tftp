package com.java.dto;

import com.java.model.Folder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FolderWithPermissionDTO {
    private Folder folder;
    private String permission;

    public FolderWithPermissionDTO(Folder folder, String permission) {
        this.folder = folder;
        this.permission = permission;
    }

}