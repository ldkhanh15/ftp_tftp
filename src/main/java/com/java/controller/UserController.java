package com.java.controller;
import com.java.Service.UserService;
import com.java.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;

@RequiredArgsConstructor
@Component
public class UserController {
    private final UserService userService;
    public boolean login(String username, String password, Role role) {
        try {
            return userService.login(username, password, role);
        } catch (Exception e){
            System.out.println(e.getMessage());
            return false;
        }
    }

    public List<Object[]> getUsers() {
        try {
            var users = userService.getAllUser();
                return users.stream().map(
                        u -> new Object[]{
                                u.getId(),
                                u.getUsername(),
                                u.getPassword(),
                                u.getRole()
                        }
                ).toList();
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
        return null;
    }
}
