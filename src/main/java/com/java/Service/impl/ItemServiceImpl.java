package com.java.Service.impl;

import com.java.Service.ItemService;
import com.java.exception.DataNotFoundException;
import com.java.model.File;
import com.java.model.Folder;
import com.java.model.Item;
import com.java.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    @Override
    public Item testItem(Long id) {
        Item item = itemRepository.findById(id).orElseThrow(
                () -> new DataNotFoundException("Item not found")
        );
        if (item instanceof Folder){
            System.out.println("Item is a Folder");
            Folder folder = (Folder) item;
            // thao tac tiep ...
        }
        else if (item instanceof File){
            System.out.println("Item is a File");
            // thao tac tiep...
        }
        System.out.println("Item is : " + item);
        return null;
    }
}
