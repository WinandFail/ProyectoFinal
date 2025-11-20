package com.example.proyectofinal.model;

public class SavingsAccount extends BankAccount {
    private final double interestRate;
    private final double minimumBalance;

    public SavingsAccount(String accountNumber, double balance, Client owner, String openingDate,
                          double interestRate, double minimumBalance) {
        super(accountNumber, balance, owner, openingDate);
        this.interestRate = interestRate;
        this.minimumBalance = minimumBalance;
    }

    @Override
    public boolean deposit(double amount) {
        if (amount > 0) {
            this.balance += amount;
            this.addTransaction(new Transaction("DEPOSIT", amount, "Savings account deposit"));
            return true;
        }
        return false;
    }

    @Override
    public boolean withdraw(double amount) {
        if (amount > 0 ) {
            this.balance -= amount;
            this.addTransaction(new Transaction("WITHDRAWAL", amount, "Savings account withdrawal"));
            return true;
        }
        return false;
    }

    @Override
    public boolean transfer(BankAccount destination, double amount) {
        if (this.withdraw(amount)) {
            destination.deposit(amount);
            this.addTransaction(new Transaction("TRANSFER", amount,
                    "Transfer to account " + destination.getAccountNumber()));
            return true;
        }
        return false;
    }

    @Override
    public String generateReport() {
        return String.format("Savings Account: %s\nBalance: $%.2f\nInterest Rate: %.2f%%\nMinimum Balance: $%.2f",
                accountNumber, balance, interestRate * 100, minimumBalance);
    }

   
    public double getInterestRate() { return interestRate; }

    public double getMinimumBalance() { return minimumBalance; }
}
