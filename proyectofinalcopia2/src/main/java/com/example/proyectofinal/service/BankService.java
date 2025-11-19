package com.example.proyectofinal.service;

import com.example.proyectofinal.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BankService {

    private final List<Client> clients;
    private final List<Employee> employees;
    private final List<BankAccount> accounts;
    private final List<Transaction> globalTransactions;
    private static AuthenticationService authService;

    public BankService() {
        this.clients = new ArrayList<>();
        this.employees = new ArrayList<>();
        this.accounts = new ArrayList<>();
        this.globalTransactions = new ArrayList<>();
        if (authService == null) {
            authService = new AuthenticationService();
        }
        initializeData();
    }

    private void initializeData() {

        Client client1 = new Client("C001", "Alejandro Campo", "alejo@email.com",
                "123456789", "Carrera 45 #12-34", "2025-01-11", "alejo.campo", "cliente123");
        Client client2 = new Client("C002", "Ana Martínez", "ana@email.com",
                "987654321", "Avenida 68 #23-45", "2025-02-11", "ana.martinez", "cliente123");

        clients.add(client1);
        clients.add(client2);

        authService.registerCredentials("alejo.campo", "cliente123", client1);
        authService.registerCredentials("ana.martinez", "cliente123", client2);

        SavingsAccount savingsAccount1 = new SavingsAccount("AHO-001", 5000000.0, client1, "2025-01-15", 0.02, 100.0);
        SavingsAccount savingsAccount2 = new SavingsAccount("AHO-002", 3000000.0, client2, "2025-02-20", 0.02, 100.0);
        CheckingAccount checkingAccount1 = new CheckingAccount("CTE-001", 1000000.0, client1, "2025-01-20", 2000000.0);
        CheckingAccount checkingAccount2 = new CheckingAccount("CTE-002", 8000000.0, client2, "2025-02-25", 1500000.0);
        BusinessAccount businessAccount = new BusinessAccount("EMP-001", 5000000.0, client1, "2025-01-25",
                "Universidad Del Quindío", "900123456-7", 10000000.0);

        accounts.add(savingsAccount1);
        accounts.add(savingsAccount2);
        accounts.add(checkingAccount1);
        accounts.add(checkingAccount2);
        accounts.add(businessAccount);

        client1.addAccount(savingsAccount1);
        client1.addAccount(checkingAccount1);
        client1.addAccount(businessAccount);
        client2.addAccount(savingsAccount2);
        client2.addAccount(checkingAccount2);


        employees.add(new Employee("EMP001", "Sara Rodríguez", "sara@bancouq.com",
                "555111222", "Calle 80 #15-20", "Cajero", 2500000.0, "2025-10-10",
                "sara.rodriguez", "cajero123"));


        authService.registerCredentials("sara.rodriguez", "cajero123", employees.getFirst());



        globalTransactions.add(new Transaction("DEPOSITO", 1000000.0, "Depósito inicial"));
        globalTransactions.add(new Transaction("RETIRO", 500000.0, "Retiro de cajero"));
    }

    public void registerClient(Client client) {
        clients.add(client);

        String accountNumber = "AHO-" + String.format("%03d", clients.size());
        SavingsAccount defaultAccount = new SavingsAccount(accountNumber, 0.0, client,
                java.time.LocalDate.now().toString(), 0.02, 100.0);

        registerAccount(defaultAccount);

        authService.registerCredentials(client.getUser(), client.getPassword(), client);
    }
    public void deleteClient(String clientId) {
        clients.removeIf(client -> client.getId().equals(clientId));

        accounts.removeIf(account -> account.getHolder().getId().equals(clientId));
    }

    public Client findClientById(String id) {
        return clients.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public List<Client> getAllClients() {
        return new ArrayList<>(clients);
    }

    public void registerEmployee(Employee employee) {
        employees.add(employee);
        authService.registerCredentials(employee.getUser(), employee.getPassword(), employee);
    }

    public boolean deleteEmployee(String id) {
        return employees.removeIf(e -> e.getId().equals(id));
    }

    public List<Employee> getAllEmployees() {
        return new ArrayList<>(employees);
    }

    public BankAccount findAccountByNumber(String accountNumber) {
        return accounts.stream()
                .filter(c -> c.getAccountNumber().equals(accountNumber))
                .findFirst()
                .orElse(null);
    }

    public List<BankAccount> getAccountsByClient(String clientId) {
        return accounts.stream()
                .filter(c -> c.getHolder().getId().equals(clientId))
                .collect(Collectors.toList());
    }

    public void registerAccount(BankAccount account) {
        accounts.add(account);
        account.getHolder().addAccount(account);
    }

    public List<BankAccount> getAllAccounts() {
        return new ArrayList<>(accounts);
    }

    public void registerGlobalTransaction(Transaction transaction) {
        globalTransactions.add(transaction);
    }

    public List<Transaction> getSuspiciousTransactions() {
        return globalTransactions.stream()
                .filter(t -> t.getAmount() > 10000000)
                .collect(Collectors.toList());
    }

    public List<Transaction> getAllTransactions() {
        return new ArrayList<>(globalTransactions);
    }

    public String generateClientReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== REPORTE DE CLIENTES ===\n");
        report.append(String.format("Total clientes: %d\n\n", clients.size()));

        for (Client client : clients) {
            report.append(String.format("ID: %s\nNombre: %s\nEmail: %s\nTel: %s\n",
                    client.getId(), client.getName(), client.getEmail(), client.getPhone()));

            List<BankAccount> clientAccounts = getAccountsByClient(client.getId());
            report.append(String.format("Número de cuentas: %d\n", clientAccounts.size()));

            for (BankAccount account : clientAccounts) {
                report.append(String.format("  - %s: $%.2f\n",
                        account.getAccountNumber(), account.getBalance()));
            }
            report.append("\n");
        }
        return report.toString();
    }

    public String generateTransactionReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== REPORTE DE TRANSACCIONES ===\n");
        report.append(String.format("Total transacciones: %d\n\n", globalTransactions.size()));

        for (Transaction transaction : globalTransactions) {
            report.append(transaction.toString()).append("\n");
        }
        return report.toString();
    }

    public String generateAccountReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== REPORTE DE CUENTAS ===\n");
        report.append(String.format("Total cuentas: %d\n\n", accounts.size()));

        for (BankAccount account : accounts) {
            report.append(account.generateReport()).append("\n\n");
        }
        return report.toString();
    }

    public AuthenticationService getAuthService() {
        return authService;
    }
}