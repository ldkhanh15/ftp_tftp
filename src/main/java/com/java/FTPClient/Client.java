package com.java.FTPClient;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

public class Client extends JFrame {

    private JTree localTree, remoteTree;
    private JTable localTable, remoteTable;
    private JTextArea logArea;

    public Client() {
        setTitle("FTP Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);  // Increase frame size to fit all components
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
        logArea = new JTextArea(50, 20);
        logArea.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(logArea);

        // Create a JPanel to hold the log area with a fixed size
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.add(logScrollPane, BorderLayout.CENTER);
        logPanel.setPreferredSize(new Dimension(1200, 150)); // Set a fixed size for the log panel

        // Create local tree and table
        localTree = createLocalTree();
        localTable = createFileTable();

        // Create remote tree and table
        remoteTree = createRemoteTree();
        remoteTable = createFileTableRemote();

        // Add double-click listener to trees
        addTreeListenersRemote();

        // Add double-click listener to trees
        addTreeListeners();

        // Split panes for trees and tables
        JSplitPane localSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(localTree), new JScrollPane(localTable));
        JSplitPane remoteSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(remoteTree), new JScrollPane(remoteTable));
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                localSplitPane, remoteSplitPane);

        // Main layout
        setLayout(new BorderLayout());
        add(connectionPanel, BorderLayout.NORTH);
        add(mainSplitPane, BorderLayout.CENTER);
        add(logPanel, BorderLayout.SOUTH); // Add the logPanel here

        setVisible(true);
        System.out.println("GUI is visible!");
    }

    private JTree createLocalTree() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("This Computer");
        File[] roots = File.listRoots(); // Lấy danh sách các ổ đĩa

        if (roots != null) {
            for (File rootDrive : roots) {
                DefaultMutableTreeNode driveNode = new DefaultMutableTreeNode(rootDrive.getPath());
                driveNode.add(new DefaultMutableTreeNode("Loading...")); // Placeholder để tải lười
                root.add(driveNode);
            }
        }

        DefaultTreeModel model = new DefaultTreeModel(root);
        JTree tree = new JTree(model);

        // Lắng nghe sự kiện mở rộng để tải nội dung ổ đĩa
        tree.addTreeWillExpandListener(new javax.swing.event.TreeWillExpandListener() {
            @Override
            public void treeWillExpand(javax.swing.event.TreeExpansionEvent event) throws javax.swing.tree.ExpandVetoException {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
                if (node.getChildCount() == 1 && node.getChildAt(0).toString().equals("Loading...")) {
                    node.removeAllChildren(); // Xóa placeholder
                    File file = new File(node.toString());
                    loadFileSystemLazy(node, file);
                    ((DefaultTreeModel) tree.getModel()).reload(node);
                }
            }

            @Override
            public void treeWillCollapse(javax.swing.event.TreeExpansionEvent event) throws javax.swing.tree.ExpandVetoException {
                // Xử lý sự kiện thu gọn nếu cần
            }
        });

        return tree;
    }

    private void loadFileSystemLazy(DefaultMutableTreeNode parentNode, File file) {
        File[] files = file.listFiles(File::isDirectory); // Only list directories for lazy loading
        if (files != null) {
            for (File f : files) {
                DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(f.getName());
                childNode.add(new DefaultMutableTreeNode("Loading...")); // Placeholder
                parentNode.add(childNode);
            }
        }
    }

    private JTable createFileTable() {
        String[] columnNames = {"Name", "Size", "Type", "Last Modified"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        return new JTable(model);
    }
    private JTable createFileTableRemote() {
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
                    updateFileTableRemote(remoteTable, folderName);
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
    private JTree createTree(String rootName) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(rootName);
        DefaultTreeModel model = new DefaultTreeModel(root);
        return new JTree(model);
    }

    private void addTreeListeners() {
        localTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    DefaultMutableTreeNode selectedNode =
                            (DefaultMutableTreeNode) localTree.getLastSelectedPathComponent();
                    if (selectedNode != null) {
                        File selectedFile = getFileFromNode(selectedNode, new File(System.getProperty("user.home")));
                        if (selectedFile != null && selectedFile.isDirectory()) {
                            logArea.append("Local folder selected: " + selectedFile.getAbsolutePath() + "\n");
                            updateFileTable(localTable, selectedFile);
                        }
                    }
                }
            }
        });
    }
    private void addTreeListenersRemote() {
        remoteTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    DefaultMutableTreeNode selectedNode =
                            (DefaultMutableTreeNode) remoteTree.getLastSelectedPathComponent();
                    if (selectedNode != null) {
                        String selectedFolder = selectedNode.toString();
                        logArea.append("Remote folder selected: " + selectedFolder + "\n");
                        updateFileTableRemote(remoteTable, selectedFolder);  // Update the table with folder contents
                    }
                }
            }
        });
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
                        updateFileTableRemote(remoteTable, selectedFolder);
                    }
                }
            }
        });

        return tree;
    }
    private void updateFileTableRemote(JTable table, String folderName) {
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
    private File getFileFromNode(DefaultMutableTreeNode node, File baseDir) {
        StringBuilder path = new StringBuilder(node.getUserObject().toString());
        while ((node = (DefaultMutableTreeNode) node.getParent()) != null && node.getParent() != null) {
            path.insert(0, node.getUserObject() + File.separator);
        }
        return new File(path.toString());
    }


    private void updateFileTable(JTable table, File folder) {
        String[] columnNames = {"Name", "Size", "Type", "Last Modified"};
        File[] files = folder.listFiles();
        Object[][] data = {};
        if (files != null) {
            data = new Object[files.length][4];
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

    public static void main(String[] args) {
        try {
            SwingUtilities.invokeLater(Client::new);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
