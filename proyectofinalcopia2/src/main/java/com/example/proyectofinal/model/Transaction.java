package com.example.proyectofinal.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction {
    private String id;
    private final String type;
    private final double amount;
    private final String date;
    private final String description;

    public Transaction(String type, double amount, String description) {
        this.id = generateId();
        this.type = type;
        this.amount = amount;
        this.date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.description = description;
    }

    private String generateId() {
        return "TXN" + System.currentTimeMillis();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }

    public double getAmount() { return amount; }

    public String getDate() { return date; }

    public String getDescription() { return description; }

    @Override
    public String toString() {
        return String.format("%s - %s: $%.2f - %s", date, type, amount, description);
    }
}