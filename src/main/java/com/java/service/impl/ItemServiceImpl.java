package com.java.service.impl;

import com.java.service.ItemService;
import com.java.dto.NodeDTO;
import com.java.enums.ItemType;
import com.java.exception.DataNotFoundException;
import com.java.model.File;
import com.java.model.Folder;
import com.java.model.Item;
import com.java.repository.FolderRepository;
import com.java.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Component
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final FolderRepository folderRepository;

    @Override
    public List<NodeDTO> getItemByParentId(Long id) {
        List<Item> items = itemRepository.findAll();
        List<NodeDTO> res = new ArrayList<>();

        items.stream().forEach(item -> {
            if (item instanceof Folder folder){
                if ((folder.getParentFolder() != null && folder.getParentFolder().getItemId().equals(id))
                    || (folder.getParentFolder() == null && id == -1)) {
                    res.add(
                            NodeDTO.builder()
                                    .id(folder.getItemId())
                                    .name(folder.getFolderName())
                                    .itemType(ItemType.FOLDER)
                                    .build()
                    );
                }
            } else if (item instanceof File file) {
                if (file.getParentFolder() != null && file.getParentFolder().getItemId().equals(id)
                    || (file.getParentFolder() == null && id == -1)) {
                    res.add(
                            NodeDTO.builder()
                                    .id(file.getItemId())
                                    .name(file.getFileName())
                                    .itemType(ItemType.FILE)
                                    .build()
                    );
                }
            }
        });

        return res;
    }

    @Override
    public long getFolderRootId() {
        Folder folder = folderRepository.findByParentFolder_ItemId(null).orElseThrow(
                () -> new DataNotFoundException("Folder not found")
        );
        return folder.getItemId();
    }
}
