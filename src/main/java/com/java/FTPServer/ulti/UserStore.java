package com.java.FTPServer.ulti;

import com.java.FTPServer.system.Client;
import lombok.Getter;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

public class UserStore {
    @Getter
    private static final List<Client> clients = new ArrayList<>();
    private static final PropertyChangeSupport support = new PropertyChangeSupport(UserStore.class);

    public static void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public static void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    private static final List<PropertyChangeListener> clientListeners = new ArrayList<>();

    public static synchronized void addClient(Client client) {
        if (!clients.contains(client)) {
            clients.add(client);

            // Tạo lắng nghe sự kiện
            PropertyChangeListener clientListener = evt -> {
                if ("userSession".equals(evt.getPropertyName())) {
                    System.out.println("User session updated: " + evt.getNewValue());
                    support.firePropertyChange("clients", null, clients); // Fire event để cập nhật giao diện
                }
                if ("loggedIn".equals(evt.getPropertyName()) && (boolean) evt.getNewValue()) {
                    System.out.println("User logged in: " + client.getUserSession().getUsername());
                    support.firePropertyChange("clients", null, clients); // Fire event để cập nhật giao diện
                }
            };

            // Thêm lắng nghe vào client và lưu tham chiếu
            client.addPropertyChangeListener(clientListener);
            clientListeners.add(clientListener);

            support.firePropertyChange("clients", null, clients);
        }
    }

    public static synchronized void removeClient(Client client) {
        if (clients.contains(client)) {
            int index = clients.indexOf(client);

            // Hủy lắng nghe sự kiện
            if (index != -1) {
                PropertyChangeListener listener = clientListeners.get(index);
                client.removePropertyChangeListener(listener);
                clientListeners.remove(index);
            }

            clients.remove(client);
            support.firePropertyChange("clients", null, clients);
        }

        System.out.println("Client removed. Current clients: " + clients.stream()
                .map(c -> c.getUserSession().getDataPort() + " - " + c.getUserSession().getUsername())
                .toList());
    }
}

