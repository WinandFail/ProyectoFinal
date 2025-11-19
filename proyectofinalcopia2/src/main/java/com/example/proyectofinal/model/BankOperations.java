package com.example.proyectofinal.model;

public interface BankOperations {
    boolean deposit(double amount);
    boolean withdraw(double amount);
    boolean transfer(BankAccount destination, double amount);
    double checkBalance();
    String generateReport();
}