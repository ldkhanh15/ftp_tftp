package com.java.Service;

import com.java.enums.Role;
import com.java.model.User;

import java.util.List;

public interface UserService {
    boolean login(String username, String password, Role role);
    List<Object[]> getAllUser();
}
