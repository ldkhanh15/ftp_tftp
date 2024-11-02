package com.java.controller;

import com.java.service.ItemService;
import com.java.dto.NodeDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;


@Component
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;
    public List<NodeDTO> getChildrenByParentId(long parentId) {
        try {
            return itemService.getItemByParentId(parentId);
        } catch (Exception e){
            System.out.println(e.getMessage());
            return null;
        }
    }
    public long getRootId() {
        try {
            return itemService.getFolderRootId();
        } catch (Exception e){
            System.out.println(e.getMessage());
            return -1;
        }
    }
}









// test
//        List<NodeDTO> nodes = new ArrayList<>();
//        if (parentId == -1) {
//            nodes.add(NodeDTO.builder()
//                    .id(1L)
//                    .name("folder 1")
//                    .itemType(ItemType.FOLDER)
//                    .path("/folder1")
//                    .build());
//            nodes.add(NodeDTO.builder()
//                    .id(3L)
//                    .name("folder 3")
//                    .itemType(ItemType.FOLDER)
//                    .path("/folder3")
//                    .build());
//            return nodes;
//        }
//        nodes.add(NodeDTO.builder()
//                .id(10L)
//                .name("folder con 1")
//                .itemType(ItemType.FOLDER)
//                .path("/folder2")
//                .build());
//        nodes.add(NodeDTO.builder()
//                .id(11L)
//                .name("folder con 2")
//                .itemType(ItemType.FOLDER)
//                .path("/folder2")
//                .build());
//        nodes.add(NodeDTO.builder()
//                .id(19L)
//                .name("day la file ne")
//                .itemType(ItemType.FILE)
//                .path("/folder2")
//                .build());
//        return nodes;
