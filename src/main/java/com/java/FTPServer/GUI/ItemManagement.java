package com.java.FTPServer.GUI;

import com.java.FTPServer.system.ConstFTP;
import com.java.controller.AccessItemController;
import com.java.controller.FileController;
import com.java.controller.FolderController;
import com.java.controller.UserController;
import com.java.enums.AccessType;
import com.java.model.AccessItem;
import com.java.model.Folder;
import com.java.model.User;
import lombok.Setter;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class ItemManagement {
    private static JTree remoteTree;
    private static JTable remoteTable;
    private static String folderRoot = ConstFTP.ROOT_DIR;
    private final FileController fileController;
    private final FolderController folderController;
    private final UserController userController;
    private final AccessItemController accessItemController;
    @Setter
    private User user;


    public ItemManagement(FileController fileController, FolderController folderController, UserController userController, AccessItemController accessItemController) {
        this.fileController = fileController;
        this.folderController = folderController;
        this.userController = userController;
        this.accessItemController = accessItemController;
    }

    public JPanel createFileFolderPanel() {
        // Create remote tree and table
        remoteTree = createRemoteTree();
        remoteTable = createFileTable();


        // Add double-click listener to trees
        addTreeListeners();

        JPanel filePanel = new JPanel();
        filePanel.setLayout(new BorderLayout());

        // Split pane for remote tree and table
        JSplitPane remoteSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(remoteTree), new JScrollPane(remoteTable));
        filePanel.add(remoteSplitPane, BorderLayout.CENTER);

        return filePanel;
    }

    private JTree createRemoteTree() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(folderRoot);
        File rootDir = new File(folderRoot);
        populateTree(root, rootDir);

        DefaultTreeModel model = new DefaultTreeModel(root);
        JTree tree = new JTree(model);
        tree.setRowHeight(20);
        tree.setDragEnabled(true);
        // Tạo popup menu
        JPopupMenu popupMenu = new JPopupMenu();

        // Thêm mục "Tạo mới folder"
        JMenuItem createFolderItem = new JMenuItem("Tạo mới folder");
        createFolderItem.addActionListener(e -> {
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) remoteTree.getLastSelectedPathComponent();
            File folderParent = getFolderFromNode(selectedNode);
            if (selectedNode != null) {
                String folderName = JOptionPane.showInputDialog(remoteTree, "Nhập tên folder mới:");
                if (folderName != null && !folderName.isEmpty()) {
                    File newFolder = new File(folderParent, folderName);
                    if (newFolder.mkdir()) {
                        // Chỉ lưu tên folder làm userObject
                        DefaultMutableTreeNode newFolderNode = new DefaultMutableTreeNode(folderName);
                        selectedNode.add(newFolderNode);
                        ((DefaultTreeModel) remoteTree.getModel()).reload(selectedNode);

                        // Cập nhật bảng ngay lập tức
                        updateFileTable(remoteTable, folderParent);
                        folderController.save(folderParent.getAbsolutePath(), folderName,user.getUsername());
                    }
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
                    File folderToRename = getFolderFromNode(selectedNode);
                    System.out.println("name: " + folderToRename.getName());
                    System.out.println("new name: " + newName);
                    Optional<Folder> folderParentOpt =
                            folderController.findFolderParentByPath(folderToRename.getPath());
                    System.out.println("path: "+ folderToRename.getPath());
                    if (folderParentOpt.isPresent()) {
                        Folder folderParent = folderParentOpt.get();
                        Optional<Folder> folderIsExistOpt =
                                folderController.findFolderByFolderNameAndParentFolder(newName,
                                folderParent);
                        if (folderIsExistOpt.isPresent()) {
                            JOptionPane.showMessageDialog(tree, "Folder name is the same");
                        } else {
                            Optional<Folder> folderOpt = folderController.findFolderIdByPath(folderToRename.getAbsolutePath());
                            if (folderOpt.isPresent()) {
                                File renamedFolder = new File(folderToRename.getParent(), newName);
                                Folder folder = folderOpt.get();
                                folder.setFolderName(renamedFolder.getName());
                                folderController.save(folder);
                                if (folderToRename.renameTo(renamedFolder)) {
                                    selectedNode.setUserObject(renamedFolder.getName());
                                    ((DefaultTreeModel) tree.getModel()).reload(selectedNode);
                                }
                            } else {
                                JOptionPane.showMessageDialog(tree, "Folder not found");
                            }
                        }
                    }else{
                        JOptionPane.showMessageDialog(tree, "Folder parent not found");
                    }
                }
            }
        });
        popupMenu.add(renameFolderItem);

        JMenuItem createFileItem = new JMenuItem("Tạo mới File");
        createFileItem.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();

            int result = fileChooser.showOpenDialog(remoteTree);
            DefaultMutableTreeNode selectedNode =
                    (DefaultMutableTreeNode) remoteTree.getLastSelectedPathComponent();

            if (selectedNode != null) {
                // Resolve the folder/file from the tree node
                File selectedFile = getFileFromNode(selectedNode);

                if (selectedFile != null && selectedFile.isDirectory()) {
                    if (result == JFileChooser.APPROVE_OPTION) {
                        // Get the selected file to upload
                        File uploadFile = fileChooser.getSelectedFile();
                        System.out.println("Selected file to upload: " + uploadFile.getAbsolutePath());

                        // Create the destination file path (selected folder + file name)
                        File destinationFile = new File(selectedFile, uploadFile.getName());

                        try {
                            // Check if the file already exists in the destination folder
                            if (!destinationFile.exists()) {
                                // Copy the file to the selected folder
                                Files.copy(uploadFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                fileController.save(selectedFile.getAbsolutePath(), uploadFile);
                            } else {
                                System.out.println("File already exists in the destination folder.");
                            }
                            updateFileTable(remoteTable, selectedFile);

                        } catch (IOException ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(remoteTree, "Error uploading file: " + ex.getMessage());
                        }
                    }
                }
            }
        });
        popupMenu.add(createFileItem);

        JMenuItem deleteItem = new JMenuItem("Xóa");
        deleteItem.addActionListener(e -> {
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (selectedNode != null) {
                File folder = getFileFromNode(selectedNode);
                int confirm = JOptionPane.showConfirmDialog(remoteTree, "Bạn có chắc chắn muốn xóa folder này?", "Xác " +
                        "nhận xóa", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    deleteFolder(folder);
                    updateFileTable(remoteTable, folder);
                    model.removeNodeFromParent(selectedNode);
                    Optional<Folder> folderDB = folderController.findFolderIdByPath(folder.getAbsolutePath());
                    if (folderDB.isPresent()) {
                        folderController.deleteById(folderDB.get().getItemId());
                    }
                }
            }
        });
        popupMenu.add(deleteItem);
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

        return tree;
    }

    private JTable createFileTable() {
        String[] columnNames = {"Name", "Size", "Type", "Last Modified"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(model);
        table.setRowHeight(20);
        fileTablePopup(table, remoteTree);
        return table;
    }

    private File getFolderFromNode(DefaultMutableTreeNode node) {
        if (node == null) {
            return null; // Node không hợp lệ
        }

        // Xây dựng đường dẫn từ root đến node hiện tại
        StringBuilder pathBuilder = new StringBuilder(folderRoot); // Bắt đầu từ root
        TreeNode[] pathNodes = node.getPath(); // Lấy tất cả các node từ root đến node hiện tại

        for (int i = 1; i < pathNodes.length; i++) { // Bỏ qua root node (index 0)
            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) pathNodes[i];
            Object userObject = treeNode.getUserObject();
            if (userObject != null) {
                pathBuilder.append(File.separator).append(userObject.toString());
            }
        }

        return new File(pathBuilder.toString());
    }


    private void updateFileTable(JTable table, File folder) {
        String[] columnNames = {"Name", "Size", "Type", "Last Modified"};
        File[] files = folder.listFiles();

        Object[][] data = new Object[files != null ? files.length : 0][4];
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                data[i][0] = f.getName();
                data[i][1] = f.isFile() ? f.length() + " bytes" : "-";
                data[i][2] = f.isDirectory() ? "Folder" : "File";
                data[i][3] = new Date(f.lastModified());
            }
        }
        DefaultTableModel model = new DefaultTableModel(data, columnNames);
        table.setModel(model);
    }

    private void fileTablePopup(JTable table, JTree tree) {
        // Tạo popup menu
        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem createFolderItem = new JMenuItem("Tạo mới folder");
        createFolderItem.addActionListener(e -> {
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) remoteTree.getLastSelectedPathComponent();
            File folderParent = getFolderFromNode(selectedNode);
            if (selectedNode != null) {
                String folderName = JOptionPane.showInputDialog(remoteTree, "Nhập tên folder mới:");
                if (folderName != null && !folderName.isEmpty()) {
                    File newFolder = new File(folderParent, folderName);
                    if (newFolder.mkdir()) {
                        // Chỉ lưu tên folder làm userObject
                        DefaultMutableTreeNode newFolderNode = new DefaultMutableTreeNode(folderName);
                        selectedNode.add(newFolderNode);
                        ((DefaultTreeModel) remoteTree.getModel()).reload(selectedNode);

                        // Cập nhật bảng ngay lập tức
                        updateFileTable(remoteTable, folderParent);
                        folderController.save(folderParent.getAbsolutePath(), folderName,user.getUsername());
                    }
                }
            }
        });
        popupMenu.add(createFolderItem);

        JMenuItem createFileItem = new JMenuItem("Tạo mới File");
        createFileItem.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();

            int result = fileChooser.showOpenDialog(table);
            DefaultMutableTreeNode selectedNode =
                    (DefaultMutableTreeNode) remoteTree.getLastSelectedPathComponent();

            if (selectedNode != null) {
                // Resolve the folder/file from the tree node
                File selectedFile = getFileFromNode(selectedNode);

                if (selectedFile != null && selectedFile.isDirectory()) {
                    if (result == JFileChooser.APPROVE_OPTION) {
                        // Get the selected file to upload
                        File uploadFile = fileChooser.getSelectedFile();
                        System.out.println("Selected file to upload: " + uploadFile.getAbsolutePath());

                        // Create the destination file path (selected folder + file name)
                        File destinationFile = new File(selectedFile, uploadFile.getName());

                        try {
                            // Check if the file already exists in the destination folder
                            if (!destinationFile.exists()) {
                                // Copy the file to the selected folder
                                Files.copy(uploadFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                fileController.save(selectedFile.getAbsolutePath(), uploadFile);
                            } else {
                                System.out.println("File already exists in the destination folder.");
                            }
                            updateFileTable(table, selectedFile);

                        } catch (IOException ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(table, "Error uploading file: " + ex.getMessage());
                        }
                    }
                }
            }
        });
        popupMenu.add(createFileItem);

        // Thêm mục "Xóa"
        JMenuItem deleteItem = new JMenuItem("Xóa");
        deleteItem.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                String fileName = (String) table.getValueAt(selectedRow, 0);
                String fileType = (String) table.getValueAt(selectedRow, 2);

                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                if (selectedNode != null) {
                    File folder = getFileFromNode(selectedNode);
                    File fileToDelete = new File(folder, fileName);

                    // Kiểm tra nếu file là thư mục
                    if (fileType.equals("Folder")) {
                        int confirm = JOptionPane.showConfirmDialog(table, "Bạn có chắc chắn muốn xóa folder này?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
                        if (confirm == JOptionPane.YES_OPTION) {
                            deleteFolder(fileToDelete);
                            updateFileTable(table, folder);
                            Optional<Folder> folderDB = folderController.findFolderIdByPath(fileToDelete.getAbsolutePath());
                            if (folderDB.isPresent()) {
                                folderController.deleteById(folderDB.get().getItemId());
                            }
                        }
                    } else {
                        int confirm = JOptionPane.showConfirmDialog(table, "Bạn có chắc chắn muốn xóa file này?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
                        if (confirm == JOptionPane.YES_OPTION) {
                            if (fileToDelete.delete()) {
                                JOptionPane.showMessageDialog(table, "File đã được xóa!");
                                updateFileTable(table, folder);
                                Optional<Folder> parent =
                                        folderController.findFolderIdByPath(fileToDelete.getParentFile().getAbsolutePath());
                                if (parent.isPresent()) {
                                    com.java.model.File file = fileController.findByFileNameAndFolderParent(fileToDelete.getName(),
                                            parent.get());
                                    fileController.deleteById(file.getItemId());
                                }
                            } else {
                                JOptionPane.showMessageDialog(table, "Không thể xóa file.");
                            }
                        }
                    }
                }
            }
        });
        popupMenu.add(deleteItem);

        JMenuItem permissionItem = new JMenuItem("Phân quyền");
        permissionItem.addActionListener(e -> {
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (selectedNode != null) {
                JDialog permissionDialog = new JDialog((Frame) null, "Phân quyền truy cập", true);
                permissionDialog.setLayout(new BorderLayout());
                permissionDialog.setSize(500, 500);

                // Panel tìm kiếm người dùng
                JPanel searchPanel = new JPanel(new BorderLayout());
                JTextField searchField = new JTextField();
                JButton searchButton = new JButton("Tìm kiếm");
                searchPanel.add(new JLabel("Tìm người dùng:"), BorderLayout.WEST);
                searchPanel.add(searchField, BorderLayout.CENTER);
                searchPanel.add(searchButton, BorderLayout.EAST);

                // Danh sách tìm kiếm kết quả
                DefaultListModel<String> searchListModel = new DefaultListModel<>();
                JList<String> searchList = new JList<>(searchListModel);
                List<User> users = userController.findAll();
                Long itemId = 0L;
                for (User user : users) {
                    searchListModel.addElement(user.getUsername());
                }
                JScrollPane searchScrollPane = new JScrollPane(searchList);
                searchScrollPane.setBorder(BorderFactory.createTitledBorder("Kết quả tìm kiếm"));

                // Bảng hiển thị danh sách người dùng được phân quyền
                List<AccessItem> accessItems = null;
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    String fileName = (String) table.getValueAt(selectedRow, 0);
                    String fileType = (String) table.getValueAt(selectedRow, 2);

                    if (selectedNode != null) {
                        File folder = getFileFromNode(selectedNode);
                        File fileToDelete = new File(folder, fileName);

                        // Kiểm tra nếu file là thư mục
                        if (fileType.equals("Folder")) {
                            Optional<Folder> folderDB = folderController.findFolderIdByPath(fileToDelete.getAbsolutePath());
                            if (folderDB.isPresent()) {
                                itemId = folderDB.get().getItemId();
                                accessItems = accessItemController.getAccessItemsByItem(folderDB.get());
                            }
                        } else {
                            Optional<Folder> parent =
                                    folderController.findFolderIdByPath(fileToDelete.getParentFile().getAbsolutePath());
                            if (parent.isPresent()) {
                                com.java.model.File file = fileController.findByFileNameAndFolderParent(fileToDelete.getName(),
                                        parent.get());
                                itemId = file.getItemId();
                                accessItems = accessItemController.getAccessItemsByItem(file);
                            }
                        }
                    }
                }
                DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"Username", "Quyền"}, 0);
                JTable userTable = new JTable(tableModel);
                for (AccessItem accessItem : accessItems) {
                    tableModel.addRow(new Object[]{accessItem.getUser().getUsername(), accessItem.getAccessType()});
                }

                // Dropdown chọn quyền
                JComboBox<String> permissionComboBox = new JComboBox<>(new String[]{"READ", "WRITE", "ALL"});
                userTable.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(permissionComboBox));

                JScrollPane tableScrollPane = new JScrollPane(userTable);

                // Nút thêm người dùng từ danh sách tìm kiếm vào bảng phân quyền
                JButton addUserButton = new JButton("Thêm người dùng");
                addUserButton.addActionListener(event -> {
                    String selectedUser = searchList.getSelectedValue();
                    if (selectedUser != null) {
                        // Kiểm tra nếu người dùng đã tồn tại trong bảng
                        boolean alreadyAdded = false;
                        for (int i = 0; i < tableModel.getRowCount(); i++) {
                            if (tableModel.getValueAt(i, 0).equals(selectedUser)) {
                                alreadyAdded = true;
                                break;
                            }
                        }
                        if (!alreadyAdded) {
                            tableModel.addRow(new Object[]{selectedUser, "READ"}); // Thêm mặc định quyền là READ
                        } else {
                            JOptionPane.showMessageDialog(permissionDialog, "Người dùng đã có trong danh sách.");
                        }
                    } else {
                        JOptionPane.showMessageDialog(permissionDialog, "Vui lòng chọn người dùng từ danh sách tìm kiếm.");
                    }
                });

                // Xử lý tìm kiếm người dùng
                searchButton.addActionListener(event -> {
                    String searchQuery = searchField.getText().trim();
                    searchListModel.clear();

                    if (!searchQuery.isEmpty()) {
                        List<User> userSearchList = (userController.searchByUsername(searchQuery));
                        for (User user : userSearchList) {
                            searchListModel.addElement(user.getUsername());
                        }

                        if (searchListModel.isEmpty()) {
                            JOptionPane.showMessageDialog(permissionDialog, "Không tìm thấy người dùng nào.");
                        }
                    } else {
                        for (User user : users) {
                            searchListModel.addElement(user.getUsername());
                        }
                    }
                });

                // Panel chứa nút Apply và Cancel
                JPanel actionPanel = new JPanel(new FlowLayout());
                JButton applyButton = new JButton("Áp dụng");
                JButton cancelButton = new JButton("Hủy");

                // Xử lý nút Apply
                Long finalItemId = itemId;
                applyButton.addActionListener(event -> {
                    for (int i = 0; i < tableModel.getRowCount(); i++) {
                        String username = (String) tableModel.getValueAt(i, 0);
                        String permission = (String) tableModel.getValueAt(i, 1);
                        accessItemController.addAccess(username, finalItemId, AccessType.valueOf(permission));
                    }

                    // Thông báo danh sách phân quyền
                    JOptionPane.showMessageDialog(permissionDialog,
                            "Danh sách user da duoc them quyen thanh cong");
                    permissionDialog.dispose();
                });

                // Xử lý nút Cancel
                cancelButton.addActionListener(event -> permissionDialog.dispose());

                actionPanel.add(applyButton);
                actionPanel.add(cancelButton);

                // Panel chứa danh sách tìm kiếm và nút thêm
                JPanel searchResultPanel = new JPanel(new BorderLayout());
                searchResultPanel.add(searchScrollPane, BorderLayout.CENTER);
                searchResultPanel.add(addUserButton, BorderLayout.SOUTH);

                // Chia layout chính
                JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, searchResultPanel, tableScrollPane);
                splitPane.setResizeWeight(0.5);

                // Thêm các thành phần vào dialog
                permissionDialog.add(searchPanel, BorderLayout.NORTH);
                permissionDialog.add(splitPane, BorderLayout.CENTER);
                permissionDialog.add(actionPanel, BorderLayout.SOUTH);

                permissionDialog.setLocationRelativeTo(tree);
                permissionDialog.setVisible(true);
            }
        });
        popupMenu.add(permissionItem);
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
    }

    private void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteFolder(file);
                } else {
                    file.delete();
                }
            }
        }
        folder.delete();
    }

    private void addTreeListeners() {
        remoteTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { // Kiểm tra double-click
                    DefaultMutableTreeNode selectedNode =
                            (DefaultMutableTreeNode) remoteTree.getLastSelectedPathComponent();

                    if (selectedNode != null) {
                        Object userObject = selectedNode.getUserObject();
                        if (userObject instanceof TreeNodeData) { // Kiểm tra kiểu
                            TreeNodeData nodeData = (TreeNodeData) userObject;
                            File selectedFile = nodeData.file;

                            if (selectedFile.isDirectory()) {
                                // Cập nhật bảng hiển thị nội dung thư mục
                                updateFileTable(remoteTable, selectedFile);

                                // Nếu chưa tải các nút con, thêm chúng
                                if (!nodeData.loaded) {
                                    populateTree(selectedNode, selectedFile);
                                    nodeData.loaded = true; // Đánh dấu đã tải
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    private void populateTree(DefaultMutableTreeNode parentNode, File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    TreeNodeData data = new TreeNodeData(file, false); // Tạo TreeNodeData
                    DefaultMutableTreeNode node = new DefaultMutableTreeNode(data);
                    parentNode.add(node);
                }
            }
        }
    }

    // Lớp giữ dữ liệu thư mục và trạng thái đã tải hay chưa
    private static class TreeNodeData {
        File file;
        boolean loaded;

        TreeNodeData(File file, boolean loaded) {
            this.file = file;
            this.loaded = loaded;
        }

        @Override
        public String toString() {
            return file.getName(); // Để hiển thị tên trong JTree
        }
    }

    private File getFileFromNode(DefaultMutableTreeNode node) {
        StringBuilder path = new StringBuilder();
        while (node != null && node.getUserObject() != null) {
            path.insert(0, node.getUserObject().toString() + File.separator);
            node = (DefaultMutableTreeNode) node.getParent();
        }

        if (path.length() > 0 && path.charAt(path.length() - 1) == File.separatorChar) {
            path.deleteCharAt(path.length() - 1);
        }
        return new File(path.toString());
    }

}
