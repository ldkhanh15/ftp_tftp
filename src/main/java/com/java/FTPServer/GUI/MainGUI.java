package com.java.FTPServer.GUI;

import com.java.configuration.AppConfig;
import com.java.controller.UserController;
import com.java.enums.Role;
import com.java.model.Item;
import com.java.model.User;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
@Component
@Scope("prototype")
public class MainGUI extends JFrame {
    private JTextArea logArea;
    private JPanel sidebarPanel;
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private final UserController userController;
    private User user;
    public MainGUI(UserController userController) {
        this.userController = userController;
    }

    public void init () {
        setTitle("Server");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 600);
        setLocationRelativeTo(null);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

//        // Create connection panel
        JPanel connectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        connectionPanel.add(new JLabel("Username:"));
        JTextField usernameField = new JTextField(10);
        connectionPanel.add(usernameField);
        connectionPanel.add(new JLabel("Password:"));
        JPasswordField passwordField = new JPasswordField(10);
        connectionPanel.add(passwordField);

        JButton loginButton = new JButton("Login");
        connectionPanel.add(loginButton);


        // Create log area
        logArea = new JTextArea(5, 20);
        logArea.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(logArea);

        // Create Sidebar
        sidebarPanel = createSidebarPanel();

        // Create CardLayout for content panel to switch between different views (folders, users, logs)
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        // Add remote tree and table to content panel
        //JPanel filePanel = ItemManagement.createFileFolderPanel();
        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        UserManagement userManagement = context.getBean(UserManagement.class);
        JPanel userManagementPanel = userManagement.createUserManagementPanel();

        JPanel logPanel = Log.createLogPanel();

        ItemManagement itemManagement = context.getBean(ItemManagement.class);
        JPanel filePanel = itemManagement.createFileFolderPanel();

        ActiveConnection activeConnection = context.getBean(ActiveConnection.class);
        JPanel activeConnectionsPanel = activeConnection.createActiveConnectionsPanel();

        // Add panels to content
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
        sidebarPanel.setVisible(false);
        contentPanel.setVisible(false);

// Login button functionality
        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            // Replace with actual authentication logic
            this.user=authenticateUser(username, password);
            if (this.user != null) {
                refreshFilePanel();
                JOptionPane.showMessageDialog(this, "Login Successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                connectionPanel.setVisible(true); // Hide the login panel
                sidebarPanel.setVisible(true);    // Show the sidebar
                contentPanel.setVisible(true);    // Show the content

            } else {
                JOptionPane.showMessageDialog(this, "Invalid Credentials. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    private User authenticateUser(String username, String password) {
        User user=userController.findByUsername(username);
        if(user!=null && user.getPassword().equals(password) && user.getRole() == Role.ADMIN){
            return user;
        }
        return null;
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

        btnFileManagement.addActionListener(e -> {
            // Add logic to refresh or update data for file management
            cardLayout.show(contentPanel, "File Management");
            refreshFilePanel();
        });
        btnUserManagement.addActionListener(e -> {
            // Add logic to refresh or update data for user management
            cardLayout.show(contentPanel, "User Management");
            refreshUserPanel();
        });
        btnLogManagement.addActionListener(e -> {
            // Add logic to refresh or update data for log management
            cardLayout.show(contentPanel, "Log Management");
            refreshLogPanel();
        });
        btnActiveConnections.addActionListener(e -> {
            // Add logic to refresh or update data for active connections
            cardLayout.show(contentPanel, "Active Connections");
            refreshActiveConnectionsPanel();
        });

        sidebar.setLayout(null);
        sidebar.add(btnFileManagement);
        sidebar.add(btnUserManagement);
        sidebar.add(btnLogManagement);
        sidebar.add(btnActiveConnections);

        return sidebar;
    }

    // Refresh file management panel
    private void refreshFilePanel() {
        // Logic to refresh log content, e.g. reload logs
        System.out.println("Refreshing Item");
        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        // Recreate the active connections panel to update its contents
        ItemManagement item = context.getBean(ItemManagement.class);
        item.setUser(user);
        JPanel itemPanel = item.createFileFolderPanel();

        contentPanel.removeAll();
        contentPanel.add(itemPanel, "item");

        cardLayout.show(contentPanel, "item");
        revalidate();  // Revalidate the layout to reflect changes
        repaint();  // Repaint to update the UI
    }

    // Refresh user management panel
    private void refreshUserPanel() {
        // Logic to refresh log content, e.g. reload logs
        System.out.println("Refreshing Log Management");
        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        // Recreate the active connections panel to update its contents
        UserManagement log = context.getBean(UserManagement.class);
        JPanel userManagementPanel = log.createUserManagementPanel();

        contentPanel.removeAll();
        contentPanel.add(userManagementPanel, "userManagementPanel");

        cardLayout.show(contentPanel, "userManagementPanel");
        revalidate();  // Revalidate the layout to reflect changes
        repaint();  // Repaint to update the UI
    }

    // Refresh log panel
    private void refreshLogPanel() {
        // Logic to refresh log content, e.g. reload logs
        System.out.println("Refreshing Log Management");
        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        // Recreate the active connections panel to update its contents
        Log log = context.getBean(Log.class);
        JPanel logPanel = log.createLogPanel();

        contentPanel.removeAll();
        contentPanel.add(logPanel, "Log");

        cardLayout.show(contentPanel, "Log");
        revalidate();  // Revalidate the layout to reflect changes
        repaint();  // Repaint to update the UI
    }

    private void refreshActiveConnectionsPanel() {
        System.out.println("Refreshing Active Connections");
        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        ActiveConnection activeConnection = context.getBean(ActiveConnection.class);
        JPanel activeConnectionsPanel = activeConnection.createActiveConnectionsPanel();

        contentPanel.removeAll();
        contentPanel.add(activeConnectionsPanel, "Active Connections");

        cardLayout.show(contentPanel, "Active Connections");
        revalidate();
        repaint();
    }
}
