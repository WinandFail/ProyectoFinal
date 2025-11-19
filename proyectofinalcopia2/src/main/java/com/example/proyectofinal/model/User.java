package com.example.proyectofinal.model;

public abstract class User {
    protected String id;
    protected String name;
    protected String email;
    protected String phone;
    protected String address;

    public User(String id, String name, String email, String phone, String address) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }

    public String getEmail() { return email; }

    public String getPhone() { return phone; }

    public String getAddress() { return address; }
}