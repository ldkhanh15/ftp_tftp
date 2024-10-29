package com.java.GUI.view;

import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionListener;

@Component
public class UserListView {
    private JFrame frame;
    private JTable table;
    private JButton viewButton;

    UserListView(){
        init();
    }

    private void init() {
        // Tạo JFrame cho danh sách người dùng
        frame = new JFrame("User List");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 300);

        // Dữ liệu cho bảng người dùng
        String[] columnNames = {"ID", "Username", "Name", "Role"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        table = new JTable(model);
        table.setVisible(false); // Ẩn bảng ban đầu

        // Thêm bảng vào JScrollPane
        JScrollPane scrollPane = new JScrollPane(table);
        frame.add(scrollPane);

        // Tạo và thêm nút "Xem"
        viewButton = new JButton("Xem");
        viewButton.addActionListener(e -> showUserTable());
        frame.add(viewButton, "South"); // Thêm nút vào phía dưới

        // Hiển thị frame sau khi thêm tất cả các thành phần
        frame.setVisible(false);
    }

    public void setUserData(Object[][] data) {
        System.err.println("helloooooooooooooooooooooooooooooooooooooooooooooo");
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0); // Xóa dữ liệu cũ
        for (Object[] user : data) {
            model.addRow(user);
        }
    }

    private void showUserTable() {
        table.setVisible(true); // Hiện bảng khi nút "Xem" được nhấn
        frame.revalidate(); // Cập nhật lại giao diện để hiển thị bảng
    }

    public void setVisible(boolean visible) {
        frame.setVisible(visible);
    }

    public void addViewButtonListener(ActionListener listener) {
        viewButton.addActionListener(listener); // Thêm ActionListener cho nút "Xem"
    }
}
