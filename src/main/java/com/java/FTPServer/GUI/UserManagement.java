package com.java.FTPServer.GUI;

import com.java.enums.Role;
import com.java.model.User;
import com.java.service.UserService;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

@Component
public class UserManagement {

    private final UserService userService;

    public UserManagement(UserService userService) {
        this.userService = userService;
    }

    public JPanel createUserManagementPanel() {
        JPanel userPanel = new JPanel(new BorderLayout());

        String[] columnNames = {"User_id", "Username", "Name", "Role", "Status"};

        // Create the table model
        DefaultTableModel userTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Disables cell editing
            }
        };

        JTable userTable = new JTable(userTableModel);
        JScrollPane tableScrollPane = new JScrollPane(userTable);
        userPanel.add(tableScrollPane, BorderLayout.CENTER);

        // Form for adding/updating user details
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("User Details"));
        JTextField txtUsername = new JTextField();
        JTextField txtName = new JTextField();
        JComboBox<Role> roleComboBox = new JComboBox<>(Role.values());
        JPasswordField txtPassword = new JPasswordField();
        JPasswordField txtConfirmPassword = new JPasswordField();
        JTextField txtUserId=new JTextField();
        txtUserId.setEnabled(false);

        formPanel.add(new JLabel("User id"));
        formPanel.add(txtUserId);
        formPanel.add(new JLabel("Username:"));
        formPanel.add(txtUsername);
        formPanel.add(new JLabel("Name:"));
        formPanel.add(txtName);
        formPanel.add(new JLabel("Password:"));
        formPanel.add(txtPassword);
        formPanel.add(new JLabel("Confirm Password:"));
        formPanel.add(txtConfirmPassword);
        formPanel.add(new JLabel("Role:"));
        formPanel.add(roleComboBox);

        // Buttons for CRUD operations
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAdd = new JButton("Add");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");
        JButton btnClear = new JButton("Clear");
        JButton btnChangePassword = new JButton("Change Password");
        JButton btnActivate = new JButton("Activate");
        JButton btnDeactivate = new JButton("Deactivate");

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);
        buttonPanel.add(btnChangePassword);
        buttonPanel.add(btnActivate);
        buttonPanel.add(btnDeactivate);

        // Add listeners for buttons
        btnAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = txtUsername.getText();
                String name = txtName.getText();
                String password = new String(txtPassword.getPassword());
                String confirmPassword = new String(txtConfirmPassword.getPassword());
                Role role = (Role) roleComboBox.getSelectedItem();

                if (username.isEmpty() || name.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    JOptionPane.showMessageDialog(userPanel, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Check if passwords match
                if (!password.equals(confirmPassword)) {
                    JOptionPane.showMessageDialog(userPanel, "Passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Check if the username already exists
                if (userService.findByUsername(username) != null) {
                    JOptionPane.showMessageDialog(userPanel, "Username already exists. Please choose another username.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                User newUser = new User();
                newUser.setUsername(username);
                newUser.setName(name);
                newUser.setRole(role);
                newUser.setPassword(password);
                newUser.setIsEnabled(true); // Mặc định người dùng là active
                userService.save(newUser);
                refreshTable(userTableModel);
                clearForm(txtUserId,txtUsername, txtName, txtPassword, txtConfirmPassword);
            }
        });
        btnUpdate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = userTable.getSelectedRow();
                if (selectedRow >= 0) {
                    String username = txtUsername.getText();
                    String name = txtName.getText();
                    Role role = (Role) roleComboBox.getSelectedItem();
                    User user = userService.findByUsername(username);
                    if (user != null) {
                        user.setName(name); // Update name
                        user.setRole(role);
                        userService.save(user);
                        refreshTable(userTableModel); // Refresh table after update
                        clearForm(txtUserId,txtUsername, txtName, txtPassword, txtConfirmPassword);
                    }
                } else {
                    JOptionPane.showMessageDialog(userPanel, "Please select a user to update.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        btnDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = userTable.getSelectedRow();
                if (selectedRow >= 0) {
                    String username = txtUsername.getText();
                    userService.deleteUserByUsername(username);
                    refreshTable(userTableModel); // Refresh table after delete
                    clearForm(txtUserId,txtUsername, txtName, txtPassword, txtConfirmPassword);
                } else {
                    JOptionPane.showMessageDialog(userPanel, "Please select a user to delete.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        btnClear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                txtUsername.setEnabled(true);
                txtPassword.setEnabled(true);
                txtConfirmPassword.setEnabled(true);
                clearForm(txtUserId,txtUsername, txtName, txtPassword, txtConfirmPassword);
            }
        });

        btnChangePassword.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = userTable.getSelectedRow();
                if (selectedRow >= 0) {
                    String username = txtUsername.getText();
                    User user = userService.findByUsername(username);
                    if (user != null) {
                        // Show password change dialog
                        String oldPassword = JOptionPane.showInputDialog(userPanel, "Enter old password:");
                        String newPassword = JOptionPane.showInputDialog(userPanel, "Enter new password:");
                        String confirmNewPassword = JOptionPane.showInputDialog(userPanel, "Confirm new password:");

                        if (oldPassword != null && !oldPassword.isEmpty() && newPassword != null && !newPassword.isEmpty() && confirmNewPassword != null && !confirmNewPassword.isEmpty()) {
                            if (user.getPassword().equals(oldPassword)) {
                                if (newPassword.equals(confirmNewPassword)) {
                                    user.setPassword(newPassword); // Set the new password
                                    userService.save(user);
                                    JOptionPane.showMessageDialog(userPanel, "Password changed successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                                } else {
                                    JOptionPane.showMessageDialog(userPanel, "New passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
                                }
                            } else {
                                JOptionPane.showMessageDialog(userPanel, "Incorrect old password.", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        } else {
                            JOptionPane.showMessageDialog(userPanel, "Password fields cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(userPanel, "Please select a user to change the password.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        btnActivate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = userTable.getSelectedRow();
                if (selectedRow >= 0) {
                    String username = userTable.getValueAt(selectedRow, 1).toString();
                    User user = userService.findByUsername(username);
                    if (user != null) {
                        user.setIsEnabled(true); // Kích hoạt người dùng
                        userService.save(user);
                        refreshTable(userTableModel);
                    }
                }
            }
        });

        btnDeactivate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = userTable.getSelectedRow();
                if (selectedRow >= 0) {
                    String username = userTable.getValueAt(selectedRow, 1).toString();
                    User user = userService.findByUsername(username);
                    if (user != null) {
                        user.setIsEnabled(false); // Vô hiệu hóa người dùng
                        userService.save(user);
                        refreshTable(userTableModel);
                    }
                }
            }
        });

        userTable.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = userTable.getSelectedRow();
            if (selectedRow >= 0) {
                txtUsername.setEnabled(false);
                txtPassword.setEnabled(false);
                txtConfirmPassword.setEnabled(false);
                txtUserId.setText(userTable.getValueAt(selectedRow,0).toString());
                txtUsername.setText(userTable.getValueAt(selectedRow, 1).toString());
                txtName.setText(userTable.getValueAt(selectedRow, 2).toString());
                roleComboBox.setSelectedItem(Role.valueOf(userTable.getValueAt(selectedRow, 3).toString()));
            }
        });

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(formPanel, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        userPanel.add(bottomPanel, BorderLayout.SOUTH);

        refreshTable(userTableModel);

        return userPanel;
    }

    private void clearForm(JTextField txtUserId,JTextField txtUsername, JTextField txtName, JPasswordField txtPassword,
                           JPasswordField txtConfirmPassword) {
        txtUsername.setText("");
        txtName.setText("");
        txtPassword.setText("");
        txtConfirmPassword.setText("");
        txtUserId.setText("");
        txtUsername.setEnabled(true);
        txtPassword.setEnabled(true);
        txtConfirmPassword.setEnabled(true);
    }

    private void refreshTable(DefaultTableModel userTableModel) {
        userTableModel.setRowCount(0);
        List<User> users = userService.findAll();
        for (User user : users) {
            userTableModel.addRow(new Object[]{
                    user.getId(),
                    user.getUsername(),
                    user.getName(),
                    user.getRole(),
                    user.getIsEnabled() ? "Active" : "Inactive"
            });
        }
    }
}
