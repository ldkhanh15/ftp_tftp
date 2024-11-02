package com.java.service.impl;

import com.java.service.UserService;
import com.java.enums.Role;
import com.java.exception.DataNotFoundException;
import com.java.model.User;
import com.java.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public boolean login(String username, String password, Role role) {
        var user = findByUsername(username);
        return user.getRole() == role && user.getPassword().equals(password);
    }

    @Override
    public List<Object[]> getAllUser() {
        return userRepository.findAll().stream().map(
                u -> new Object[]{
                        u.getId(),
                        u.getUsername(),
                        u.getPassword(),
                        u.getRole()
                }
        ).toList();
    }

    private User findByUsername(String username){
        return userRepository.findByUsername(username).orElseThrow(
                () -> new DataNotFoundException("User with username " + username + " not found")
        );
    }
}
