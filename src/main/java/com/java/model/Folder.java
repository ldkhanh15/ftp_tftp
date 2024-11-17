package com.java.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Folder extends Item implements Serializable {

    @Column(name = "folder_name", nullable = false, length = 255)
    private String folderName;
    @OneToMany(mappedBy = "parentFolder")
    private Set<Folder> subFolders;

    @OneToMany(mappedBy = "parentFolder")
    private List<File> files;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Folder parentFolder;
}
