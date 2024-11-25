package com.java.service;

import com.java.dto.UserDTO;
import com.java.enums.Role;
import com.java.model.User;

import java.util.List;

public interface UserService {
    boolean login(String username, String password);
    List<Object[]> getAllUser();
    List<User> findAll();
    User findByUsername(String username);
    UserDTO findByUserNameDTO(String username);
    User findUserById(Long userId);
    User save(User user);
    void deleteUser(Long userId);

    void deleteUserByUsername(String username);

    List<User> searchByUsername(String username);
}
