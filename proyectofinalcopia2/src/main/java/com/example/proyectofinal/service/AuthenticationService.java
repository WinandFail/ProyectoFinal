package com.example.proyectofinal.service;

import com.example.proyectofinal.model.Administrator;
import com.example.proyectofinal.model.User;

import java.util.HashMap;
import java.util.Map;

public class AuthenticationService {
    private final Map<String, String> credentials;
    private final Map<String, User> users;

    public AuthenticationService() {
        this.credentials = new HashMap<>();
        this.users = new HashMap<>();
        initializeUsers();
    }

    private void initializeUsers() {

        Administrator admin = new Administrator("ADM001", "Admin Principal", "admin@banco.com",
                "123456789", "Sede Central", "Administrador", 5000000.0, "2024-01-01", "TOTAL",
                "admin", "admin123");
        credentials.put("admin", "admin123");
        users.put("admin", admin);

    }

    public User authenticate(String user, String password) {
        if (credentials.containsKey(user) && credentials.get(user).equals(password)) {
            return users.get(user);
        }
        return null;
    }

    public void registerCredentials(String user, String password, User userObj) {
        credentials.put(user, password);
        users.put(user, userObj);
    }

    public boolean userExists(String user) {
        return users.containsKey(user);
    }
}