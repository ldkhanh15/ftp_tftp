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

    public static synchronized void addClient(Client client) {
        boolean isExist = false;
        for (Client client1 : clients) {
            if (client1.equals(client)) {
                isExist = true;
                break;
            }
        }
        if (!isExist) {
            clients.add(client);  // Thêm client vào UserStore nếu chưa có
            support.firePropertyChange("clients", null, clients);  // Cập nhật thông tin
        }
        System.out.println("in store: "+clients.size());
    }


    public static synchronized void removeClient(Client client) {
        if (clients.contains(client)) {
            clients.remove(client);
            support.firePropertyChange("clients", null, clients);
        }
        System.out.println("Client removed. Current clients: " + clients.stream()
                .map(c -> c.getUserSession().getDataPort() + " - " + c.getUserSession().getUsername())
                .toList());
    }
}
