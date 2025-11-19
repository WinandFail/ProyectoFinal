package com.example.proyectofinal.model;

public class Administrator extends Employee {
    private final String accessLevel;

    public Administrator(String id, String name, String email, String phone, String address,
                         String position, double salary, String hireDate, String accessLevel,
                         String user, String password) {
        super(id, name, email, phone, address, position, salary, hireDate, user, password);
        this.accessLevel = accessLevel;
    }

    public String getAccessLevel() { return accessLevel; }

}