package com.example.proyectofinal.model;

public class BusinessAccount extends BankAccount {
    private final String companyName;
    private final String nit;
    private final double transferLimit;

    public BusinessAccount(String accountNumber, double balance, Client holder, String openingDate,
                           String companyName, String nit, double transferLimit) {
        super(accountNumber, balance, holder, openingDate);
        this.companyName = companyName;
        this.nit = nit;
        this.transferLimit = transferLimit;
    }

    @Override
    public boolean deposit(double amount) {
        if (amount > 0) {
            this.balance += amount;
            this.addTransaction(new Transaction("DEPOSITO", amount, "Depósito a cuenta empresarial"));
            return true;
        }
        return false;
    }

    @Override
    public boolean withdraw(double amount) {
        if (amount > 0 && amount <= this.balance) {
            this.balance -= amount;
            this.addTransaction(new Transaction("RETIRO", amount, "Retiro de cuenta empresarial"));
            return true;
        }
        return false;
    }

    @Override
    public boolean transfer(BankAccount destination, double amount) {
        if (amount <= transferLimit && this.withdraw(amount)) {
            destination.deposit(amount);
            this.addTransaction(new Transaction("TRANSFERENCIA", amount,
                    "Transferencia a cuenta " + destination.getAccountNumber()));
            return true;
        }
        return false;
    }

    @Override
    public String generateReport() {
        return String.format("Cuenta Empresarial: %s\nEmpresa: %s\nNIT: %s\nSaldo: $%.2f\nLímite Transferencia: $%.2f",
                accountNumber, companyName, nit, balance, transferLimit);
    }


    public String getCompanyName() { return companyName; }

    public String getNit() { return nit; }

    public double getTransferLimit() { return transferLimit; }

}