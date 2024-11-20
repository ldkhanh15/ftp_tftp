package com.java.FTPServer.GUI;

import com.java.FTPServer.system.ConstFTP;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@Component
public class ItemManagement {
    private static JTree remoteTree;
    private static JTable remoteTable;
    private static String folderRoot = ConstFTP.ROOT_DIR;

    public static JPanel createFileFolderPanel() {
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

    private static JTree createRemoteTree() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(folderRoot);
        File rootDir=new File(folderRoot);
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
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (selectedNode != null) {
                String folderName = JOptionPane.showInputDialog(tree, "Nhập tên folder mới:");
                if (folderName != null && !folderName.isEmpty()) {
                    File parentFolder = getFolderFromNode(selectedNode);
                    File newFolder = new File(parentFolder, folderName);
                    if (newFolder.mkdir()) {
                        DefaultMutableTreeNode newFolderNode = new DefaultMutableTreeNode(newFolder.getName());
                        selectedNode.add(newFolderNode);
                        ((DefaultTreeModel) tree.getModel()).reload(selectedNode);
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
                    File renamedFolder = new File(folderToRename.getParent(), newName);
                    if (folderToRename.renameTo(renamedFolder)) {
                        selectedNode.setUserObject(renamedFolder.getName());
                        ((DefaultTreeModel) tree.getModel()).reload(selectedNode);
                    }
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

        return tree;
    }

    private static void populateTree(DefaultMutableTreeNode parentNode, File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(file.getName());
                parentNode.add(node);

                // If it's a directory, recursively populate its children
                if (file.isDirectory()) {
                    populateTree(node, file);
                }
            }
        }
    }

    private static File getFolderFromNode(DefaultMutableTreeNode node) {
        Object userObject = node.getUserObject();
        // You should map the userObject back to the File object (maybe you can store the File reference as a user object)
        return new File(folderRoot, userObject.toString());
    }

    private static void updateFileTable(JTable table, File folder) {
        String[] columnNames = {"Name", "Size", "Type", "Last Modified"};
        File[] files = folder.listFiles();

        // Debugging: Check if files are returned correctly
        System.out.println("Listing files in folder: " + folder.getAbsolutePath());
        if (files == null) {
            System.out.println("No files found or cannot access the folder: " + folder.getAbsolutePath());
        } else {
            System.out.println("Files found: " + files.length);
            for (File file : files) {
                System.out.println("File: " + file.getName() + " | Is Directory: " + file.isDirectory());
            }
        }

        Object[][] data = new Object[files != null ? files.length : 0][4];
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                data[i][0] = f.getName();
                data[i][1] = f.isFile() ? f.length() + " bytes" : "-";
                data[i][2] = f.isDirectory() ? "Folder" : "File";
                data[i][3] = new java.util.Date(f.lastModified());
            }
        }
        DefaultTableModel model = new DefaultTableModel(data, columnNames);
        table.setModel(model);
    }
    private static void addDeleteFunctionality(JTable table, JTree tree) {
        // Tạo popup menu
        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem createFolderItem = new JMenuItem("Tạo mới folder");
        createFolderItem.addActionListener(e -> {
            // Xử lý tạo folder
        });
        popupMenu.add(createFolderItem);

        JMenuItem createFileItem = new JMenuItem("Tạo mới File");
        createFileItem.addActionListener(e -> {
            // Xử lý tạo file
        });
        popupMenu.add(createFileItem);

        // Thêm mục "Xóa"
        JMenuItem deleteItem = new JMenuItem("Xóa");
        deleteItem.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                String fileName = (String) table.getValueAt(selectedRow, 0);  // Lấy tên file
                String fileType = (String) table.getValueAt(selectedRow, 2);  // Kiểm tra loại file (Folder/File)

                // Lấy folder hiện tại từ cây
                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                if (selectedNode != null) {
                    File folder = getFileFromNode(selectedNode);
                    File fileToDelete = new File(folder, fileName);

                    // Kiểm tra nếu file là thư mục
                    if (fileType.equals("Folder")) {
                        int confirm = JOptionPane.showConfirmDialog(table, "Bạn có chắc chắn muốn xóa folder này?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
                        if (confirm == JOptionPane.YES_OPTION) {
                            deleteFolder(fileToDelete);
                            updateFileTable(table, folder);  // Cập nhật lại bảng sau khi xóa
                        }
                    } else {
                        int confirm = JOptionPane.showConfirmDialog(table, "Bạn có chắc chắn muốn xóa file này?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
                        if (confirm == JOptionPane.YES_OPTION) {
                            if (fileToDelete.delete()) {
                                JOptionPane.showMessageDialog(table, "File đã được xóa!");
                                updateFileTable(table, folder);  // Cập nhật lại bảng sau khi xóa
                            } else {
                                JOptionPane.showMessageDialog(table, "Không thể xóa file.");
                            }
                        }
                    }
                }
            }
        });
        popupMenu.add(deleteItem);

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

    private static void deleteFolder(File folder) {
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
    private static JTable createFileTable() {
        String[] columnNames = {"Name", "Size", "Type", "Last Modified"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(model);
        table.setRowHeight(20);
        // Tạo popup menu
        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem createFolderItem = new JMenuItem("Tạo mới folder");
        createFolderItem.addActionListener(e -> {
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) remoteTree.getLastSelectedPathComponent();
            if (selectedNode != null) {
                String folderName = JOptionPane.showInputDialog(remoteTree, "Nhập tên folder mới:");
                if (folderName != null && !folderName.isEmpty()) {
                    DefaultMutableTreeNode newFolder = new DefaultMutableTreeNode(folderName);
                    selectedNode.add(newFolder);
                    ((DefaultTreeModel) remoteTree.getModel()).reload(selectedNode);
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
                                System.out.println("File uploaded successfully to " + destinationFile.getAbsolutePath());
                            } else {
                                System.out.println("File already exists in the destination folder.");
                            }
                            updateFileTable(table,selectedFile);

                        } catch (IOException ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(table, "Error uploading file: " + ex.getMessage());
                        }
                    }
                }
            }
        });
        popupMenu.add(createFileItem);

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
        addDeleteFunctionality(table, remoteTree);
        return table;
    }
    private static void addTreeListeners() {
        remoteTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    DefaultMutableTreeNode selectedNode =
                            (DefaultMutableTreeNode) remoteTree.getLastSelectedPathComponent();
                    if (selectedNode != null) {
                        // Resolve the folder/file from the tree node
                        File selectedFile = getFileFromNode(selectedNode);
                        if (selectedFile != null && selectedFile.isDirectory()) {
                            // Update the table to show the contents of the selected folder
                            updateFileTable(remoteTable, selectedFile);
                        }
                    }
                }
            }
        });
    }

    private static File getFileFromNode(DefaultMutableTreeNode node) {
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
