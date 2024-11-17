package com.java.GUI.handle;
import com.java.GUI.view.LoginView;
import com.java.GUI.view.UserListView;
import com.java.controller.UserController;
import com.java.enums.Role;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@Component
@RequiredArgsConstructor
public class LoginHandle{

    private final UserController userController;

    private final LoginView loginView;

    private final UserListView userListView;

    @PostConstruct
    private void initialize() {
        loginView.addLoginListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = loginView.getUsername();
                String password = loginView.getPassword();

                if (authenticate(username, password)) {
                    loginView.close();
                    forward();
                } else {
                    loginView.showError("Invalid credentials");
                }
            }
        });
    }

    private boolean authenticate(String username, String password) {
        return userController.login(username, password);
    }

    private void forward() {
        userListView.setVisible(true);
    }
}

