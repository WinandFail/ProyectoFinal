package com.example.proyectofinal.model;

import java.util.ArrayList;
import java.util.List;

public abstract class BankAccount implements BankOperations {
    protected String accountNumber;
    protected double balance;
    protected Client holder;
    protected String openingDate;
    protected List<Transaction> transactions;

    public BankAccount(String accountNumber, double balance, Client holder, String openingDate) {
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.holder = holder;
        this.openingDate = openingDate;
        this.transactions = new ArrayList<>();
    }


    public String getAccountNumber() { return accountNumber; }

    public double getBalance() { return balance; }

    public Client getHolder() { return holder; }

    public String getOpeningDate() { return openingDate; }

    public List<Transaction> getTransactions() { return transactions; }

    public void addTransaction(Transaction transaction) {
        this.transactions.add(transaction);
    }

    @Override
    public double checkBalance() {
        return this.balance;
    }
}