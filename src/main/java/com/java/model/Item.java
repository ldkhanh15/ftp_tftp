package com.java.model;

import com.java.FTPServer.ulti.UserSessionManager;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Set;

@Entity
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
public class Item implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long itemId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 255)
    private String createdBy;

    @Column(name = "is_public")
    private Boolean isPublic;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 255)
    private String updatedBy;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @OneToMany(mappedBy = "item")
    private Set<AccessItem> accessItems;

    @PrePersist
    public void handleBeforeCreate() {
        this.createdBy = UserSessionManager.getUserSession().getUsername() != null ? UserSessionManager.getUserSession().getUsername() : "";
        ZoneId zoneId = ZoneId.of("Asia/Bangkok");
        ZonedDateTime updatedAtWithZone = Instant.now().atZone(zoneId);
        this.createdAt = updatedAtWithZone.toLocalDateTime();
    }

    @PreUpdate
    public void handleBeforeUpdate() {
        this.updatedBy = UserSessionManager.getUserSession().getUsername() != null ? UserSessionManager.getUserSession().getUsername() : "";
        ZoneId zoneId = ZoneId.of("Asia/Bangkok"); // GMT+7
        ZonedDateTime updatedAtWithZone = Instant.now().atZone(zoneId);
        this.updatedAt = updatedAtWithZone.toLocalDateTime();
    }
}
