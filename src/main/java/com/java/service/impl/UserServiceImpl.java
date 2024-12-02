package com.java.service.impl;

import com.java.dto.UserDTO;
import com.java.service.UserService;
import com.java.enums.Role;
import com.java.exception.DataNotFoundException;
import com.java.model.User;
import com.java.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public boolean login(String username, String password) {
        var user = findByUsername(username);
        return user.getPassword().equals(password);
    }

    @Override
    public List<User> getAllUser() {
        return  userRepository.findAll();
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findByUsername(String username){
        return userRepository.findByUsername(username).orElse(null);
    }


    @Override
    public UserDTO findByUserNameDTO(String username) {
        return userRepository.findUserDTOByUsername(username).orElse(null);
    }

    @Override
    public User findUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    @Override
    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    @Override
    @Transactional
    public void deleteUserByUsername(String username) {
        userRepository.deleteByUsername(username);
    }

    @Override
    public List<User> searchByUsername(String username) {
        return userRepository.findByUsernameContaining(username);
    }
}
