package com.java.service;

import com.java.dto.NodeDTO;
import com.java.model.Item;

import java.util.List;

public interface ItemService{
    List<NodeDTO> getItemByParentId(Long id);
    long getFolderRootId();

    Item findByItemId(Long itemId);

    Item getById(Long itemId);
    Item save(Item item);
}
