package com.java.GUI.view;

import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

@Component
public class LoginView extends JFrame {
    private JTextField userField;
    private JPasswordField passwordField;
    private JButton loginButton;

    private void init() {
        // Thiết lập cửa sổ
        setTitle("Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 200);
        setLayout(new GridLayout(3, 2));

        // Tạo các thành phần giao diện
        JLabel userLabel = new JLabel("Username:");
        userField = new JTextField();
        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField();
        loginButton = new JButton("Login");

        // Thêm các thành phần vào JFrame
        add(userLabel);
        add(userField);
        add(passwordLabel);
        add(passwordField);
        add(new JLabel()); // khoảng trống
        add(loginButton);
    }

    public LoginView() {
        init();
    }

    // Phương thức để lấy tên người dùng
    public String getUsername() {
        return userField.getText();
    }

    // Phương thức để lấy mật khẩu
    public String getPassword() {
        return new String(passwordField.getPassword());
    }

    // Phương thức để đóng cửa sổ
    public void close() {
        dispose();
    }

    // Phương thức để hiển thị thông báo lỗi
    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Login Error", JOptionPane.ERROR_MESSAGE);
    }

    // Phương thức để thêm ActionListener cho nút đăng nhập
    public void addLoginListener(ActionListener listener) {
        loginButton.addActionListener(listener);
    }

    // Phương thức để kiểm tra trạng thái hiển thị của cửa sổ
    public boolean isVisible() {
        return super.isVisible();
    }

    // Phương thức để thiết lập trạng thái hiển thị của cửa sổ
    public void setVisible(boolean visible) {
        super.setVisible(visible);
    }
}
