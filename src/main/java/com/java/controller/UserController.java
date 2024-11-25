package com.java.controller;
import com.java.dto.UserDTO;
import com.java.model.User;
import com.java.service.UserService;
import com.java.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Component
public class UserController {
    private final UserService userService;
    public boolean  login(String username, String password) {
        try {
            return userService.login(username, password);
        } catch (Exception e){
            System.out.println(e.getMessage());
            return false;
        }
    }

    public List<Object[]> getUsers() {
        try {
            return userService.getAllUser();
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
        return null;
    }
    public List<User> findAll() {
        return userService.findAll();
    }

    public User findByUsername(String username){
        return userService.findByUsername(username);
    }


    public UserDTO findByUserNameDTO(String username) {
        return userService.findByUserNameDTO(username);
    }

    public User findUserById(Long userId) {
        return userService.findUserById(userId);
    }

    public User save(User user) {
        return userService.save(user);
    }


    public void deleteUser(Long userId) {
        userService.deleteUser(userId);
    }


    public void deleteUserByUsername(String username) {
        userService.deleteUserByUsername(username);
    }
    public List<User> searchByUsername(String username) {
        return userService.searchByUsername(username);
    }
}
