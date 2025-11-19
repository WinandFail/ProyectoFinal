package com.example.proyectofinal.model;

import java.util.ArrayList;
import java.util.List;

public class Client extends User {
    private final List<BankAccount> accounts;
    private final String registrationDate;
    private String user;
    private final String password;

    public Client(String id, String name, String email, String phone, String address, String registrationDate) {
        super(id, name, email, phone, address);
        this.accounts = new ArrayList<>();
        this.registrationDate = registrationDate;
        this.user = generateDefaultUser();
        this.password = "cliente123";
    }

    public Client(String id, String name, String email, String phone, String address,
                  String registrationDate, String user, String password) {
        super(id, name, email, phone, address);
        this.accounts = new ArrayList<>();
        this.registrationDate = registrationDate;
        this.user = user;
        this.password = password;
    }

    private String generateDefaultUser() {
        return this.name.toLowerCase().replace(" ", ".") + "." + this.id.toLowerCase();
    }

    public String getRegistrationDate() { return registrationDate; }

    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }

    public String getPassword() { return password; }

    public void addAccount(BankAccount account) {
        this.accounts.add(account);
    }
}