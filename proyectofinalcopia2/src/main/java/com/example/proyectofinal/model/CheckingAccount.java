package com.example.proyectofinal.model;

public class CheckingAccount extends BankAccount {
    private final double overdraftLimit;
    private static final double TRANSACTION_COST = 0.02;

    public CheckingAccount(String accountNumber, double balance, Client holder, String openingDate,
                           double overdraftLimit) {
        super(accountNumber, balance, holder, openingDate);
        this.overdraftLimit = overdraftLimit;
    }

    @Override
    public boolean deposit(double amount) {
        if (amount > 0) {
            this.balance += amount;
            this.addTransaction(new Transaction("DEPOSITO", amount, "Depósito a cuenta corriente"));
            return true;
        }
        return false;
    }

    @Override
    public boolean withdraw(double amount) {
        double amountWithCost = amount * (1 + TRANSACTION_COST);
        if (amount > 0 && (this.balance - amountWithCost) >= -overdraftLimit) {
            this.balance -= amountWithCost;
            this.addTransaction(new Transaction("RETIRO", amountWithCost,
                    String.format("Retiro de cuenta corriente (incluye costo 2%%: $%.2f)", amount * TRANSACTION_COST)));
            return true;
        }
        return false;
    }

    @Override
    public boolean transfer(BankAccount destination, double amount) {
        double amountWithCost = amount * (1 + TRANSACTION_COST);
        if (amount > 0 && (this.balance - amountWithCost) >= -overdraftLimit) {
            this.balance -= amountWithCost;
            destination.deposit(amount);

            this.addTransaction(new Transaction("TRANSFERENCIA", amountWithCost,
                    String.format("Transferencia a cuenta %s (incluye costo 2%%: $%.2f)",
                            destination.getAccountNumber(), amount * TRANSACTION_COST)));
            return true;
        }
        return false;
    }

    @Override
    public String generateReport() {
        return String.format("Cuenta Corriente: %s\nSaldo: $%.2f\nLímite Sobregiro: $%.2f\nCosto Transacciones: 2%%",
                accountNumber, balance, overdraftLimit);
    }


    public double getOverdraftLimit() { return overdraftLimit; }
}