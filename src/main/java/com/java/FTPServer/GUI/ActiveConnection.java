package com.java.FTPServer.GUI;

import com.java.FTPServer.enums.ResponseCode;
import com.java.FTPServer.system.Client;
import com.java.FTPServer.system.Server;
import com.java.FTPServer.ulti.LogHandler;
import com.java.FTPServer.ulti.UserStore;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

@org.springframework.stereotype.Component
public class ActiveConnection {
    private DefaultTableModel tableModel;
    public JPanel createActiveConnectionsPanel() {
        JPanel activeConnectionsPanel = new JPanel();
        activeConnectionsPanel.setLayout(new BorderLayout());

        List<Object[]> dataList = new ArrayList<>();

        for (Client client : UserStore.getClients()) {
            dataList.add(new Object[]{
                    client.getId(),
                    client.getControlSocket().getInetAddress().getHostAddress(),
                    client.getUserSession().getUsername(),
                    "Connected with port " + client.getControlSocket().getPort(),
                    "Ngắt kết nối",
                    "Xem log"
            });
        }

        Object[][] data = dataList.toArray(new Object[0][]);

        String[] columnNames = {"Thread ID", "IP", "Username", "Status", "Ngắt kết nối", "Xem log"};

        tableModel = new DefaultTableModel(data, columnNames) {
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
        UserStore.addPropertyChangeListener(evt -> {
            if ("clients".equals(evt.getPropertyName())) {
                updateClientTable();
            }
        });

        return activeConnectionsPanel;
    }

    public void updateClientTable() {
        List<Object[]> dataList = new ArrayList<>();
        for (Client client : Server.clients) {
           if(client.getUserSession()!=null){
               dataList.add(new Object[]{
                       client.getId(),
                       client.getControlSocket().getInetAddress().getHostAddress(),
                       client.getUserSession().getUsername(),
                       "Connected with port " + client.getControlSocket().getPort(),
                       "Ngắt kết nối",
                       "Xem log"
               });
           }
        }

        Object[][] data = dataList.toArray(new Object[0][]);
        tableModel.setDataVector(data, new String[]{"Thread ID", "IP", "Username", "Status", "Ngắt kết nối", "Xem log"});
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
            // Get the selected row
            int row = ((JTable) button.getParent()).getEditingRow();

            if (row >= 0) {
                if ("disconnect".equals(actionType)) {
                    Client client = UserStore.getClients().get(row);
                    UserStore.removeClient(client);
                    disconnectClient(client);
                } else if ("viewLogs".equals(actionType)) {
                    Client client = UserStore.getClients().get(row);
                    showLogViewer(client);
                }
                fireEditingStopped();
            }
        });
    }
    private void showLogViewer(Client client) {
        JFrame logFrame = new JFrame("User Log: " + client.getUserSession().getUsername());
        logFrame.setSize(600, 400);
        logFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JTextArea logTextArea = new JTextArea();
        logTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logTextArea);
        logFrame.add(scrollPane, BorderLayout.CENTER);

        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                // Read the log file for this client
                return LogHandler.read("logs/users", client.getUserSession().getUsername() + ".txt");
            }

            @Override
            protected void done() {
                try {
                    String logContent = get();
                    // Set the log content in the JTextArea
                    logTextArea.setText(logContent);
                } catch (Exception e) {
                    logTextArea.setText("Failed to load log file.");
                }
            }
        };

        worker.execute();

        logFrame.setVisible(true);
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

    private void disconnectClient(Client client) {
        if (client != null) {
            try {
                PrintWriter out= new PrintWriter(client.getControlSocket().getOutputStream());
                out.println(ResponseCode.SERVICE_CLOSING.getResponse());
                client.getControlSocket().close();
                UserStore.getClients().remove(client);
                JOptionPane.showMessageDialog(button, "Ngắt kết nối thành công!");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(button, "Lỗi khi ngắt kết nối!");
                e.printStackTrace();
            }
        }
    }
}

