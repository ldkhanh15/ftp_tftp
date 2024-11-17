package com.java.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FolderDTO {
    private Long itemId;
    private String folderName;
    private Long ownerId;
    private String ownerUsername;
}