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
@ToString
public class Folder extends Item implements Serializable {

    @Column(name = "folder_name", nullable = false, length = 255)
    private String folderName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Folder parentFolder;

    @OneToMany(mappedBy = "parentFolder")
    private Set<Folder> subFolders;

    @OneToMany(mappedBy = "parentFolder", fetch = FetchType.LAZY)
    private List<File> files;
}
