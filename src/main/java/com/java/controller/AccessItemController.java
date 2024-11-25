package com.java.controller;

import com.java.enums.AccessType;
import com.java.model.*;
import com.java.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class AccessItemController {
    private final AccessService accessService;
    private final UserService userService;
    private final FolderService folderService;
    private final FileService fileService;
    private final ItemService itemService;
    public AccessItem addAccess(String username,Long itemId, AccessType accessType){
        User user=userService.findByUsername(username);
        Item item=itemService.findByItemId(itemId);
        AccessItem accessItem=accessService.getByUserAndItem(user, item);
        if(accessItem==null){
            accessItem=new AccessItem();
            accessItem.setItem(item);
            accessItem.setUser(user);
        }
        accessItem.setAccessType(accessType);
        return accessService.save(accessItem);
    }
    public void removeAccess(String username, String fullPath, String name, boolean isFolder){
        User user=userService.findByUsername(username);
        Item item=null;
        Optional<Folder> parentFolder=folderService.findFolderIdByPath(fullPath);
        if(parentFolder.isPresent()){
            if(isFolder){
                Optional<Folder> folder=folderService.findFolderByFolderNameAndParentFolder(name,parentFolder.get());
                if(folder.isPresent()){
                    item=folder.get();
                }
            }else{
                item=fileService.findByFileNameAndFolderParent(name,parentFolder.get());
            }
        }
        accessService.removeAccessByUserIdAndItemId(user.getId(),item.getItemId());

    }
    public List<AccessItem> getAccessItemsByItem(Item item){
        return accessService.getAccessItemsByItem(item);
    }
}
