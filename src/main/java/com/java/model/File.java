package com.java.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class File extends Item implements Serializable {

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "file_path", nullable = false, length = 255)
    private String filePath;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "file_type", length = 255)
    private String fileType;
    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Folder parentFolder;

    public File(String name, String path, long length, String fileType) {
        this.fileName=name;
        this.filePath=path;
        this.fileSize=length;
        this.fileType=fileType;
    }
}
