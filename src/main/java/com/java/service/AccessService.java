package com.java.service;

import com.java.model.AccessItem;
import com.java.model.Item;
import com.java.model.User;

import java.util.List;

public interface AccessService {
     AccessItem save(AccessItem accessItem);
     AccessItem getByUserAndItem(User user, Item item);
     void removeAccessByUserIdAndItemId(Long userId, Long itemId);
     public List<AccessItem> getAccessItemsByItem(Item item);
}
