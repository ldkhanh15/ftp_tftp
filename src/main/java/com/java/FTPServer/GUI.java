package com.java.FTPServer;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class GUI extends JFrame {

    private JTree remoteTree;
    private JTable remoteTable;
    private JTextArea logArea;
    private JPanel sidebarPanel;
    private CardLayout cardLayout;
    private JPanel contentPanel;

    public GUI() {
        setTitle("FTP Server");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 600);
        setLocationRelativeTo(null);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Create connection panel
        JPanel connectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        connectionPanel.add(new JLabel("Host:"));
        connectionPanel.add(new JTextField(10));
        connectionPanel.add(new JLabel("Username:"));
        connectionPanel.add(new JTextField(10));
        connectionPanel.add(new JLabel("Password:"));
        connectionPanel.add(new JPasswordField(10));
        connectionPanel.add(new JLabel("Port:"));
        connectionPanel.add(new JTextField(5));
        connectionPanel.add(new JButton("Quickconnect"));

        // Create log area
        logArea = new JTextArea(5, 20);
        logArea.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(logArea);

        // Create remote tree and table
        remoteTree = createRemoteTree();
        remoteTable = createFileTable();

        // Add double-click listener to trees
        addTreeListeners();

        // Create Sidebar
        sidebarPanel = createSidebarPanel();

        // Create CardLayout for content panel to switch between different views (folders, users, logs)
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        // Add remote tree and table to content panel
        JPanel filePanel = createFileFolderPanel();
        JPanel userManagementPanel = createUserManagementPanel();
        JPanel logPanel = createLogPanel();
        JPanel activeConnectionsPanel = createActiveConnectionsPanel();

        contentPanel.add(filePanel, "File Management");
        contentPanel.add(userManagementPanel, "User Management");
        contentPanel.add(logPanel, "Log Management");
        contentPanel.add(activeConnectionsPanel, "Active Connections");

        // Layout the main components
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebarPanel, contentPanel);
        splitPane.setDividerLocation(200);

        // Main layout
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(connectionPanel, BorderLayout.NORTH);
        getContentPane().add(splitPane, BorderLayout.CENTER);

        setVisible(true);
        System.out.println("GUI is visible!");
    }

    // Create the sidebar panel with navigation buttons
    private JPanel createSidebarPanel() {
        JPanel sidebar = new JPanel();

        JButton btnFileManagement = new JButton("File Management");
        btnFileManagement.setBounds(0, 9, 135, 27);
        JButton btnUserManagement = new JButton("User Management");
        btnUserManagement.setBounds(0, 46, 135, 27);
        JButton btnLogManagement = new JButton("Log Management");
        btnLogManagement.setBounds(0, 83, 135, 27);
        JButton btnActiveConnections = new JButton("Active Connections");
        btnActiveConnections.setBounds(0, 124, 135, 27);

        btnFileManagement.addActionListener(e -> cardLayout.show(contentPanel, "File Management"));
        btnUserManagement.addActionListener(e -> cardLayout.show(contentPanel, "User Management"));
        btnLogManagement.addActionListener(e -> cardLayout.show(contentPanel, "Log Management"));
        btnActiveConnections.addActionListener(e -> cardLayout.show(contentPanel, "Active Connections"));
        sidebar.setLayout(null);

        sidebar.add(btnFileManagement);
        sidebar.add(btnUserManagement);
        sidebar.add(btnLogManagement);
        sidebar.add(btnActiveConnections);

        return sidebar;
    }

    // Create the file and folder management panel
    private JPanel createFileFolderPanel() {
        JPanel filePanel = new JPanel();
        filePanel.setLayout(new BorderLayout());

        // Split pane for remote tree and table
        JSplitPane remoteSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(remoteTree), new JScrollPane(remoteTable));
        filePanel.add(remoteSplitPane, BorderLayout.CENTER);

        return filePanel;
    }

    // Create the user management panel with CRUD functionality
    private JPanel createUserManagementPanel() {
        JPanel userPanel = new JPanel(new BorderLayout());

        String[] columnNames = {"User ID", "Username", "Role"};

        // Fake data for the table
        Object[][] data = {
                {1, "admin", "Administrator"},
                {2, "john_doe", "User"},
                {3, "jane_doe", "User"},
                {4, "vendor_123", "Vendor"},
                {5, "guest_user", "Guest"}
        };

        // Create the table model with fake data
        DefaultTableModel userTableModel = new DefaultTableModel(data, columnNames);
        JTable userTable = new JTable(userTableModel);
        JScrollPane tableScrollPane = new JScrollPane(userTable);
        userPanel.add(tableScrollPane, BorderLayout.CENTER);

        // Form for adding/updating user details
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("User Details"));
        JTextField txtUserId = new JTextField();
        JTextField txtUsername = new JTextField();
        JTextField txtRole = new JTextField();

        formPanel.add(new JLabel("User ID:"));
        formPanel.add(txtUserId);
        formPanel.add(new JLabel("Username:"));
        formPanel.add(txtUsername);
        formPanel.add(new JLabel("Role:"));
        formPanel.add(txtRole);

        // Buttons for CRUD operations
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAdd = new JButton("Add");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");
        JButton btnClear = new JButton("Clear");

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);

        // Add listeners for buttons
        btnAdd.addActionListener(e -> {
            String userId = txtUserId.getText();
            String username = txtUsername.getText();
            String role = txtRole.getText();
            if (!userId.isEmpty() && !username.isEmpty() && !role.isEmpty()) {
                userTableModel.addRow(new Object[]{userId, username, role});
                logArea.append("Added user: " + username + "\n");
                clearForm(txtUserId, txtUsername, txtRole);
            } else {
                JOptionPane.showMessageDialog(userPanel, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnUpdate.addActionListener(e -> {
            int selectedRow = userTable.getSelectedRow();
            if (selectedRow >= 0) {
                userTableModel.setValueAt(txtUserId.getText(), selectedRow, 0);
                userTableModel.setValueAt(txtUsername.getText(), selectedRow, 1);
                userTableModel.setValueAt(txtRole.getText(), selectedRow, 2);
                logArea.append("Updated user at row " + (selectedRow + 1) + "\n");
                clearForm(txtUserId, txtUsername, txtRole);
            } else {
                JOptionPane.showMessageDialog(userPanel, "Please select a user to update.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnDelete.addActionListener(e -> {
            int selectedRow = userTable.getSelectedRow();
            if (selectedRow >= 0) {
                userTableModel.removeRow(selectedRow);
                logArea.append("Deleted user at row " + (selectedRow + 1) + "\n");
                clearForm(txtUserId, txtUsername, txtRole);
            } else {
                JOptionPane.showMessageDialog(userPanel, "Please select a user to delete.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnClear.addActionListener(e -> clearForm(txtUserId, txtUsername, txtRole));

        // Add listener to populate form fields when a table row is selected
        userTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = userTable.getSelectedRow();
                if (selectedRow >= 0) {
                    txtUserId.setText(userTableModel.getValueAt(selectedRow, 0).toString());
                    txtUsername.setText(userTableModel.getValueAt(selectedRow, 1).toString());
                    txtRole.setText(userTableModel.getValueAt(selectedRow, 2).toString());
                }
            }
        });

        // Combine form and buttons
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(formPanel, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

        userPanel.add(bottomPanel, BorderLayout.SOUTH);

        return userPanel;
    }

    // Helper method to clear form fields
    private void clearForm(JTextField txtUserId, JTextField txtUsername, JTextField txtRole) {
        txtUserId.setText("");
        txtUsername.setText("");
        txtRole.setText("");
    }

    // Create the log management panel
    private JPanel createLogPanel() {
        JPanel logPanel = new JPanel();
        logPanel.setLayout(new BorderLayout());

        JTextArea logTextArea = new JTextArea(15, 50);
        logTextArea.setEditable(false);
        logTextArea.setText("Throw new Exception: No such folder or file.");
        JScrollPane logScrollPane = new JScrollPane(logTextArea);
        logPanel.add(logScrollPane, BorderLayout.CENTER);

        return logPanel;
    }

    // Create the active connections panel
    private JPanel createActiveConnectionsPanel() {
        JPanel activeConnectionsPanel = new JPanel();
        activeConnectionsPanel.setLayout(new BorderLayout());

        Object[][] data = {
                {101, "192.168.1.10", "John Doe", "Active", "Ngắt kết nối", "Xem log"},
                {102, "192.168.1.11", "Jane Smith", "Idle", "Ngắt kết nối", "Xem log"},
                {103, "192.168.1.12", "Michael Brown", "Active", "Ngắt kết nối", "Xem log"},
                {104, "192.168.1.13", "Emily Davis", "Disconnected", "Ngắt kết nối", "Xem log"}
        };

        String[] columnNames = {"Client ID", "IP", "Username", "Status", "Ngắt kết nối", "Xem log"};

        DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4 || column == 5;
            }
        };

        JTable activeConnectionsTable = new JTable(tableModel);

        activeConnectionsTable.getColumnModel().getColumn(4).setCellRenderer(new ButtonRenderer());
        activeConnectionsTable.getColumnModel().getColumn(4).setCellEditor(new ButtonEditor(new JCheckBox(), "disconnect"));
        activeConnectionsTable.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer());
        activeConnectionsTable.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor(new JCheckBox(), "viewLogs"));

        JScrollPane scrollPane = new JScrollPane(activeConnectionsTable);
        activeConnectionsPanel.add(scrollPane, BorderLayout.CENTER);

        return activeConnectionsPanel;
    }


    private JTree createRemoteTree() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Remote Files");
        DefaultMutableTreeNode folder1 = new DefaultMutableTreeNode("Folder1");
        DefaultMutableTreeNode folder2 = new DefaultMutableTreeNode("Folder2");
        root.add(folder1);
        root.add(folder2);

        DefaultTreeModel model = new DefaultTreeModel(root);
        JTree tree = new JTree(model);

        // Tạo popup menu
        JPopupMenu popupMenu = new JPopupMenu();

        // Thêm mục "Tạo mới folder"
        JMenuItem createFolderItem = new JMenuItem("Tạo mới folder");
        createFolderItem.addActionListener(e -> {
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (selectedNode != null) {
                String folderName = JOptionPane.showInputDialog(tree, "Nhập tên folder mới:");
                if (folderName != null && !folderName.isEmpty()) {
                    DefaultMutableTreeNode newFolder = new DefaultMutableTreeNode(folderName);
                    selectedNode.add(newFolder);
                    ((DefaultTreeModel) tree.getModel()).reload(selectedNode);
                }
            }
        });
        popupMenu.add(createFolderItem);

        // Thêm mục "Đổi tên folder"
        JMenuItem renameFolderItem = new JMenuItem("Đổi tên folder");
        renameFolderItem.addActionListener(e -> {
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (selectedNode != null && selectedNode != root) {
                String newName = JOptionPane.showInputDialog(tree, "Nhập tên mới:", selectedNode.toString());
                if (newName != null && !newName.isEmpty()) {
                    selectedNode.setUserObject(newName);
                    ((DefaultTreeModel) tree.getModel()).reload(selectedNode);
                }
            }
        });
        popupMenu.add(renameFolderItem);

        // Gắn popup menu vào cây
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int row = tree.getClosestRowForLocation(e.getX(), e.getY());
                    tree.setSelectionRow(row);
                    popupMenu.show(tree, e.getX(), e.getY());
                }
            }
        });

        // Thêm sự kiện double-click để load folder
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                    if (selectedNode != null) {
                        String selectedFolder = selectedNode.toString();
                        logArea.append("Remote folder selected: " + selectedFolder + "\n");
                        updateFileTable(remoteTable, selectedFolder);
                    }
                }
            }
        });

        return tree;
    }


    // Method to update the table based on the selected remote folder
    private void updateFileTable(JTable table, String folderName) {
        String[] columnNames = {"Name", "Size", "Type", "Last Modified"};

        // Dữ liệu giả cho các folder
        Object[][] data = null;
        if ("Folder1".equals(folderName)) {
            data = new Object[][] {
                    {"File1.txt", "1 KB", "File", "2024-11-17"},
                    {"File2.jpg", "2 MB", "File", "2024-11-16"},
            };
        } else if ("Folder2".equals(folderName)) {
            data = new Object[][] {
                    {"FileA.pdf", "3 MB", "File", "2024-11-15"},
                    {"FileB.docx", "1.5 MB", "File", "2024-11-14"},
            };
        }

        // Cập nhật model bảng
        DefaultTableModel model = new DefaultTableModel(data, columnNames);
        table.setModel(model);
    }


    private JTable createFileTable() {
        String[] columnNames = {"Name", "Size", "Type", "Last Modified"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(model);

        // Tạo popup menu
        JPopupMenu popupMenu = new JPopupMenu();

        // Thêm mục "Tạo mới Folder"
        JMenuItem createFolderItem = new JMenuItem("Tạo mới folder");
        createFolderItem.addActionListener(e -> {
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) remoteTree.getLastSelectedPathComponent();
            if (selectedNode != null) {
                String folderName = JOptionPane.showInputDialog(remoteTree, "Nhập tên folder mới:");
                if (folderName != null && !folderName.isEmpty()) {
                    DefaultMutableTreeNode newFolder = new DefaultMutableTreeNode(folderName);
                    selectedNode.add(newFolder);
                    ((DefaultTreeModel) remoteTree.getModel()).reload(selectedNode);

                    // Sau khi tạo folder mới, cập nhật bảng với các file giả
                    updateFileTable(remoteTable, folderName);
                }
            }
        });
        popupMenu.add(createFolderItem);

        // Thêm mục "Tạo mới File"
        JMenuItem createFileItem = new JMenuItem("Tạo mới File");
        createFileItem.addActionListener(e -> {
            String fileName = JOptionPane.showInputDialog(table, "Nhập tên file mới:");
            if (fileName != null && !fileName.isEmpty()) {
                model.addRow(new Object[]{fileName, "0 KB", "File", "2024-11-19"});
            }
        });
        popupMenu.add(createFileItem);

        // Thêm mục "Upload file"
        JMenuItem uploadFileItem = new JMenuItem("Upload file");
        uploadFileItem.addActionListener(e -> {
            String fileName = JOptionPane.showInputDialog(table, "Nhập tên file để upload:");
            if (fileName != null && !fileName.isEmpty()) {
                model.addRow(new Object[]{fileName, "0 KB", "File", "2024-11-19"});
            }
        });
        popupMenu.add(uploadFileItem);

        // Thêm mục "Đổi tên"
        JMenuItem renameFileItem = new JMenuItem("Đổi tên");
        renameFileItem.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                String currentName = (String) model.getValueAt(selectedRow, 0);
                String newName = JOptionPane.showInputDialog(table, "Nhập tên mới:", currentName);
                if (newName != null && !newName.isEmpty()) {
                    model.setValueAt(newName, selectedRow, 0);
                }
            }
        });
        popupMenu.add(renameFileItem);

        // Gắn popup menu vào bảng
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int row = table.rowAtPoint(e.getPoint());
                    table.setRowSelectionInterval(row, row);
                    popupMenu.show(table, e.getX(), e.getY());
                }
            }
        });

        return table;
    }
    private void addTreeListeners() {
        remoteTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    DefaultMutableTreeNode selectedNode =
                            (DefaultMutableTreeNode) remoteTree.getLastSelectedPathComponent();
                    if (selectedNode != null) {
                        String selectedFolder = selectedNode.toString();
                        logArea.append("Remote folder selected: " + selectedFolder + "\n");
                        updateFileTable(remoteTable, selectedFolder);  // Update the table with folder contents
                    }
                }
            }
        });
    }

    public static void main(String[] args) {
        try {
            SwingUtilities.invokeLater(GUI::new);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
    public ButtonRenderer() {
        setOpaque(true);
    }

    @Override
    public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setText((value == null) ? "" : value.toString());
        return this;
    }
}
class ButtonEditor extends DefaultCellEditor {
    private String label;
    private JButton button;
    private String actionType;

    public ButtonEditor(JCheckBox checkBox, String actionType) {
        super(checkBox);
        this.button = new JButton();
        this.button.setOpaque(true);
        this.actionType = actionType;

        button.addActionListener(e -> {
            if ("disconnect".equals(actionType)) {
                JOptionPane.showMessageDialog(button, "Ngắt kết nối thành công!");
            } else if ("viewLogs".equals(actionType)) {
                JOptionPane.showMessageDialog(button, "Hiển thị log người dùng...");
            }
            fireEditingStopped();
        });
    }

    @Override
    public java.awt.Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        label = (value == null) ? "" : value.toString();
        button.setText(label);
        return button;
    }

    @Override
    public Object getCellEditorValue() {
        return label;
    }
}
