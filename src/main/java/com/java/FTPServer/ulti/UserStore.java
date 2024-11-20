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

    public static void addClient(Client client) {
        clients.add(client);
        support.firePropertyChange("clients", null, clients);
    }

    public static void removeClient(Client client) {
        clients.remove(client);
        support.firePropertyChange("clients", null, clients);
    }
}
