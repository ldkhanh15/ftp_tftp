package com.java.dto;

import com.java.enums.ItemType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class NodeDTO {
    private long id;
    private String name;
    private ItemType itemType;

    @Override
    public String toString() {
        return this.name;
    }
}

