package com.example.proyectofinal.model;

public class Employee extends User {
    private final String position;
    private final double salary;
    private final String hireDate;
    private String user;
    private final String password;

    public Employee(String id, String name, String email, String phone, String address,
                    String position, double salary, String hireDate) {
        super(id, name, email, phone, address);
        this.position = position;
        this.salary = salary;
        this.hireDate = hireDate;
        this.user = generateDefaultUser();
        this.password = "123456";
    }

    public Employee(String id, String name, String email, String phone, String address,
                    String position, double salary, String hireDate, String user, String password) {
        super(id, name, email, phone, address);
        this.position = position;
        this.salary = salary;
        this.hireDate = hireDate;
        this.user = user;
        this.password = password;
    }

    private String generateDefaultUser() {
        return this.name.toLowerCase().replace(" ", ".") + "." + this.id.toLowerCase();
    }

    public String getPosition() { return position; }

    public String getHireDate() { return hireDate; }

    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }

    public String getPassword() { return password; }

    public double getSalary() {
        return salary;
    }
}