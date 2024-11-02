package com.java.service;

import com.java.enums.Role;

import java.util.List;

public interface UserService {
    boolean login(String username, String password, Role role);
    List<Object[]> getAllUser();
}
