package com.java.service.impl;

import com.java.model.AccessItem;
import com.java.model.Item;
import com.java.model.User;
import com.java.repository.AccessRepository;
import com.java.service.AccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccessServiceImpl implements AccessService {
    private final AccessRepository accessRepository;
    public AccessItem save(AccessItem accessItem){
        return accessRepository.save(accessItem);
    }

    @Override
    public AccessItem getByUserAndItem(User user, Item item) {
        return accessRepository.findByUserAndItem(user, item);
    }

    public AccessItem getByUserIdAndItemId(Long userId, Long itemId){
        return accessRepository.findAccessItemsByFolderIdAndUserId(userId, itemId);
    }

    @Override
    public void removeAccessByUserIdAndItemId(Long userId, Long itemId) {
        accessRepository.deleteAccessItemByItemIdAndUserId(itemId,userId);
    }
    public List<AccessItem> getAccessItemsByItem(Item item){
        return accessRepository.findByItem(item);
    }

    @Override
    @Transactional
    public void removeAccessByUserAndItem(User user, Item item) {
        accessRepository.deleteByItemAndUser(item,user);
    }
}
