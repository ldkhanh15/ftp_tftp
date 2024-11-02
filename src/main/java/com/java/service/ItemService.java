package com.java.service;

import com.java.dto.NodeDTO;

import java.util.List;

public interface ItemService{
    List<NodeDTO> getItemByParentId(Long id);
    long getFolderRootId();
}
