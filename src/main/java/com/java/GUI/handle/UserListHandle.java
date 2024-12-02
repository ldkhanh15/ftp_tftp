package com.java.GUI.handle;

import com.java.GUI.view.UserListView;
import com.java.controller.UserController;
import com.java.model.User;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UserListHandle {
    private final UserController userController;
    private final UserListView userListView;

    @PostConstruct
    private void initialize() {
        userListView.addViewButtonListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadUserData(); // Gọi phương thức tải dữ liệu khi nút "Xem" được nhấn
            }
        });
    }

    private void loadUserData() {
        List<User> users = userController.getUsers();
        Object[][] userData = new Object[users.size()][4];
//        for (int i = 0; i < users.size(); i++) {
//            userData[i] = new Object[]{users.get(i)[0], users.get(i)[1], users.get(i)[2], users.get(i)[3]};
//        }

        userListView.setUserData(userData); // Cập nhật dữ liệu cho bảng
        userListView.setVisible(true); // Hiện bảng
    }
}
